package com.jbm.game.engine.mina;

import java.net.InetSocketAddress;
import java.util.function.Consumer;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jbm.game.engine.mina.code.ProtocolCodecFactoryImpl;
import com.jbm.game.engine.mina.config.MinaClientConfig;

/**
 * mina udp 客户端
 * @author JiangBangMing
 *
 * 2018年7月10日 下午12:07:16
 */
public class MinaUdpClient implements Runnable{
	
	private static final Logger logger=LoggerFactory.getLogger(MinaUdpClient.class);
	
	private NioDatagramConnector connector=null;
	private MinaClientConfig minaClientConfig;//客户端配置
	private final IoHandler clientProtocolHandler;//消息处理器
	private ProtocolCodecFilter codecFilter;//消息过滤
	private int maxConnectCount;//最大连接数
	private Consumer<MinaClientConfig> sessionCreateCallBack;
	private ProtocolCodecFactoryImpl factory;//消息工厂
	private IoSession session;//连接会话
	
	public MinaUdpClient(MinaClientConfig minaClientConfig,IoHandler clientProtocolHandler,ProtocolCodecFactoryImpl factory) {
		super();
		this.minaClientConfig=minaClientConfig;
		this.clientProtocolHandler=clientProtocolHandler;
		this.factory=factory;
		this.codecFilter=new ProtocolCodecFilter(this.factory);
		init(clientProtocolHandler);
		setMinaClientConfig(minaClientConfig);
	}
	
	/**
	 * 广播所有连接的消息
	 * @param obj
	 */
	public void broadcastMsg(Object obj) {
		this.connector.broadcast(obj);
	}
	@Override
	public void run() {
		synchronized (this) {
			connect();
		}
	}
	public void stop() {
		synchronized (this) {
			try {
				connector.dispose();
				logger.info("Client is stoped.");
			}catch (Exception e) {
				logger.error("",e);
			}
		}
	}

	/**
	 * 状态监测
	 */
	public void checkStatus() {}{
		if(this.connector.getManagedSessionCount()<maxConnectCount||this.connector.getManagedSessions().size()<maxConnectCount) {
			connect();
		}
	}
	
	public MinaClientConfig getMinaClientConfig() {
		return minaClientConfig;
	}

	public IoSession getSession() {
		return session;
	}
	private void connect() {
		if(getMinaClientConfig()!=null) {
			logger.info("开始连接其他服务器,共["+getMinaClientConfig().getMaxConnectCount()+"]个");
			MinaClientConfig.MinaClienConnToConfig connTo=getMinaClientConfig().getConnTo();
			if(connTo==null) {
				logger.error("没有连接配置");
				return;
			}
			for(int i=0;i<getMinaClientConfig().getMaxConnectCount();i++) {
				ConnectFuture connect=this.connector.connect(new InetSocketAddress(connTo.getHost(),connTo.getPort()));
				connect.awaitUninterruptibly(10000L);
				if(!connect.isConnected()) {
					logger.warn("失败！连接到服务器:"+connTo.toString());
					break;
				}else {
					this.session=connect.getSession();
					if(sessionCreateCallBack!=null) {
						sessionCreateCallBack.accept(getMinaClientConfig());
					}
					logger.info("成功！连接到服务器:"+connTo.toString());
				}
				
			}
		}else {
			logger.error("连接配置为null");
		}
	}
	
	/**
	 * 初始化udp 连接
	 * @param clientProtocolHandler
	 */
	private void init(IoHandler clientProtocolHandler) {
		this.connector=new NioDatagramConnector();
		this.connector.getFilterChain().addLast("codec", codecFilter);
		this.connector.setHandler(clientProtocolHandler);
		this.connector.setConnectTimeoutMillis(60000L);
		this.connector.setConnectTimeoutCheckInterval(10000L);
	}
	
	private void setMinaClientConfig(MinaClientConfig minaClientConfig) {
		if(minaClientConfig==null) {
			return;
		}
		this.minaClientConfig=minaClientConfig;
		DatagramSessionConfig sc=this.connector.getSessionConfig();
		maxConnectCount=minaClientConfig.getMaxConnectCount();
		sc.setReceiveBufferSize(minaClientConfig.getReceiveBufferSize());
		sc.setSendBufferSize(minaClientConfig.getSendBufferSize());
		sc.setMaxReadBufferSize(minaClientConfig.getMaxReadSize());
		this.factory.getDecoder().setMaxReadSize(minaClientConfig.getMaxReadSize());
	}
	
}
