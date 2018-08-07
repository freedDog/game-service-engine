package com.jbm.game.engine.mina;

import java.net.InetSocketAddress;
import java.util.Map;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jbm.game.engine.mina.code.DefaultProtocolCodecFactory;
import com.jbm.game.engine.mina.code.ProtocolCodecFactoryImpl;
import com.jbm.game.engine.mina.config.MinaServerConfig;

/**
 * UDP服务器
 * @author JiangBangMing
 *
 * 2018年7月10日 下午5:16:26
 */
public class UdpServer implements Runnable{
	
	private static final Logger logger=LoggerFactory.getLogger(UdpServer.class);
	private MinaServerConfig minaServerConfig;
	private NioDatagramAcceptor acceptor;
	private IoHandler ioHandler;
	private ProtocolCodecFactoryImpl factory;
	private OrderedThreadPoolExecutor threadPool;//消息处理线程，使用有序线程池，保证所有session时间处理有序进行，比如执行消息执行，再是消息发送，最后关闭事件
	protected boolean isRunning=false;//服务器是否运行
	private Map<String, IoFilter> filters;//过滤器
	
	public UdpServer(MinaServerConfig minaServerConfig,IoHandler ioHandler) {
		super();
		this.minaServerConfig=minaServerConfig;
		this.ioHandler=ioHandler;
		acceptor=new NioDatagramAcceptor();
	}
	
	public UdpServer(MinaServerConfig minaServerConfig,IoHandler ioHandler,ProtocolCodecFactoryImpl factory) {
		this(minaServerConfig, ioHandler);
		this.factory=factory;
	}
	
	public UdpServer(MinaServerConfig minaServerConfig,IoHandler ioHandler,ProtocolCodecFactoryImpl factory,Map<String, IoFilter> filters) {
		this(minaServerConfig,ioHandler,factory);
		this.filters=filters;
	}
	
	/**
	 * 连接会话数
	 * @return
	 */
	public int getManagedSessionCount() {
		return acceptor==null?0:acceptor.getManagedSessionCount();
	}
	
	/**
	 * 广播所有连接的消息
	 * @param obj
	 */
	public void broadcastMsg(Object obj) {
		this.acceptor.broadcast(obj);
	}
	/**
	 */
	public void run() {
		synchronized (this) {
			if(!isRunning) {
				DefaultIoFilterChainBuilder chain=acceptor.getFilterChain();
				if(factory==null) {
					factory=new DefaultProtocolCodecFactory();
				}
				factory.getDecoder().setMaxReadSize(minaServerConfig.getMaxReadSize());
				factory.getEncoder().setMaxScheduledWriteMessages(minaServerConfig.getMaxScheduledWriteMessage());
				chain.addLast("codec",new ProtocolCodecFilter(factory));
				threadPool=new OrderedThreadPoolExecutor(minaServerConfig.getOrderedThreadPoolExecutorSize());
				chain.addLast("threadPool",new ExecutorFilter());
				if(this.filters!=null) {
					this.filters.forEach((key,filter) -> chain.addLast(key, filter));
				}
				DatagramSessionConfig dc=acceptor.getSessionConfig();
				dc.setReuseAddress(minaServerConfig.isReuseAddress());
				dc.setReceiveBufferSize(minaServerConfig.getReceiveBufferSize());
				dc.setSendBufferSize(minaServerConfig.getSendBufferSize());
				dc.setIdleTime(IdleStatus.READER_IDLE, minaServerConfig.getReaderIdleTime());
				dc.setIdleTime(IdleStatus.WRITER_IDLE, minaServerConfig.getWriterIdleTime());
				dc.setBroadcast(true);
				dc.setCloseOnPortUnreachable(true);
				
				acceptor.setHandler(ioHandler);
				try {
					acceptor.bind(new InetSocketAddress(minaServerConfig.getPort()));
					logger.info("已开始监听UDP端口:{}",minaServerConfig.getPort());
				}catch (Exception e) {
					logger.error("监听UDP端口:{}已被占用",minaServerConfig.getPort());
					logger.error("UDP,服务异常",e);
				}
			}
		}
	}
	
	public void stop() {
		synchronized (this) {
			if(!isRunning) {
				logger.info("Server "+minaServerConfig.getName()+" is already stoped.");
				return;
			}
			isRunning =false;
			try {
				if(threadPool!=null) {
					threadPool.shutdown();
				}
				acceptor.unbind();
				acceptor.dispose();
				logger.info("Server is stoped.");
			}catch (Exception e) {
				logger.error("",e);
			}
		}
	}
}
