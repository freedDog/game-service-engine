package com.jbm.game.engine.mina;

import java.net.InetSocketAddress;
import java.util.Map;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jbm.game.engine.mina.code.DefaultProtocolCodecFactory;
import com.jbm.game.engine.mina.code.ProtocolCodecFactoryImpl;
import com.jbm.game.engine.mina.config.MinaClientConfig;
import com.jbm.game.engine.mina.service.MinaClientService;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;

/**
 * Mina客户端
 * @author JiangBangMing
 *
 * 2018年7月6日 下午4:41:19
 */
public class MinaTcpClient implements Runnable{
	
	private static final Logger logger=LoggerFactory.getLogger(MinaTcpClient.class);
	
	private NioSocketConnector connector=null;//TCP连接
	private MinaClientConfig minaClientConfig;//客户端配置
	private final IoHandler clientProtocolHandler;//消息处理器
	private final ProtocolCodecFilter codecFilter;//消息过滤器
	private int maxConnectCount;
	private Consumer<MinaClientConfig> sessionCreateCallBack;
	private final MinaClientService service;//附属的客户端服务
	private final ProtocolCodecFactoryImpl factory;//消息工厂
	private Map<String, IoFilter> filters;//过滤器
	
	public MinaTcpClient(MinaClientService service,MinaClientConfig minaClientConfig,IoHandler clientProtocolHandler,
			ProtocolCodecFactoryImpl factory,Map<String, IoFilter> filters) {
		this.factory=factory;
		this.codecFilter=new ProtocolCodecFilter(factory);
		this.service=service;
		this.clientProtocolHandler=clientProtocolHandler;
		this.filters=filters;
		init(clientProtocolHandler);
		setMinaClientConfig(minaClientConfig);
	}
	
	public MinaTcpClient(MinaClientService service,MinaClientConfig minaClientConfig,IoHandler clientProtocolHandler,ProtocolCodecFactoryImpl factory) {
		this.factory=factory;
		this.codecFilter=new ProtocolCodecFilter(factory);
		this.service=service;
		this.clientProtocolHandler=clientProtocolHandler;
		init(clientProtocolHandler);
		setMinaClientConfig(minaClientConfig);
	}
	/**
	 * 使用默认消息解码工厂
	 * @param service
	 * @param minaClientConfig
	 * @param clientProtocolHandler
	 */
	 public MinaTcpClient(MinaClientService service,MinaClientConfig minaClientConfig,IoHandler clientProtocolHandler) {
		 this.factory=new DefaultProtocolCodecFactory();
		 codecFilter=new ProtocolCodecFilter(factory);
		 this.service=service;
		 this.clientProtocolHandler=clientProtocolHandler;
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
	
	/**
	 * 连接服务器
	 */
	public void connect() {
		if(getMinaClientConfig()!=null) {
			logger.info("开始连接其他服务器,共["+getMinaClientConfig().getMaxConnectCount()+"] 个");
			MinaClientConfig.MinaClienConnToConfig connTo=getMinaClientConfig().getConnTo();
			if(connTo==null) {
				logger.warn("没有连接配置");
				return;
			}
			for(int i=0;i<getMinaClientConfig().getMaxConnectCount();i++) {
				ConnectFuture connect=this.connector.connect(new InetSocketAddress(connTo.getHost(), connTo.getPort()));
				connect.awaitUninterruptibly(10000L);
				if(!connect.isConnected()) {
					logger.warn("失败! 连接到服务器:"+connTo.toString());
					break;
				}else {
					logger.info("成功! 连接到服务器:"+connTo.toString());
					if(sessionCreateCallBack!=null) {
						sessionCreateCallBack.accept(getMinaClientConfig());
					}
				}
			}
		}else {
			logger.warn("连接配置为null");
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
	 * 状态检测
	 */
	public void checkStatus() {
		if(this.connector.getManagedSessionCount()<maxConnectCount||this.connector.getManagedSessions().size()<maxConnectCount) {
			connect();
		}
	}
	
	/**
	 * 设置连接配置
	 * @param minaServerConfig
	 */
	public void setMinaClientConfig(MinaClientConfig minaClientConfig) {
		if(minaClientConfig==null) {
			return;
		}
		this.minaClientConfig=minaClientConfig;
		SocketSessionConfig sc=this.connector.getSessionConfig();
		maxConnectCount=minaClientConfig.getMaxConnectCount();
		sc.setReceiveBufferSize(minaClientConfig.getReceiveBufferSize());
		sc.setSendBufferSize(minaClientConfig.getSendBufferSize());
		sc.setMaxReadBufferSize(minaClientConfig.getMaxReadSize());
		this.factory.getDecoder().setMaxReadSize(minaClientConfig.getMaxReadSize());
		sc.setSoLinger(minaClientConfig.getSoLinger());
	}
	/**
	 * 初始化tcp连接
	 * @param clientProtocolHandler
	 */
	private void init(IoHandler clientProtocolHandler) {
		this.connector=new NioSocketConnector();
		DefaultIoFilterChainBuilder chain=this.connector.getFilterChain();
		chain.addLast("codec",codecFilter);
		if(this.filters!=null) {
			this.filters.forEach((key,filter) ->{
				if(key.equalsIgnoreCase("ssl")||key.equalsIgnoreCase("tls")) {//ssl过滤器必须添加到首部
					chain.addFirst(key, filter);
				}else {
					chain.addLast(key, filter);
				}
			});
		}
		this.connector.setHandler(clientProtocolHandler);
		this.connector.setConnectTimeoutMillis(60000L);
		this.connector.setConnectTimeoutCheckInterval(10000);
	}

	public MinaClientConfig getMinaClientConfig() {
		return minaClientConfig;
	}

	public Consumer<MinaClientConfig> getSessionCreateCallBack() {
		return sessionCreateCallBack;
	}

	public void setSessionCreateCallBack(Consumer<MinaClientConfig> sessionCreateCallBack) {
		this.sessionCreateCallBack = sessionCreateCallBack;
	}

	public IoHandler getClientProtocolHandler() {
		return clientProtocolHandler;
	}

	public MinaClientService getService() {
		return service;
	}
	
	

	
}
