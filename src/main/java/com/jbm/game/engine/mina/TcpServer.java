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
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jbm.game.engine.mina.code.DefaultProtocolCodecFactory;
import com.jbm.game.engine.mina.code.ProtocolCodecFactoryImpl;
import com.jbm.game.engine.mina.config.MinaServerConfig;

/**
 * TCP 服务器
 * @author JiangBangMing
 *
 * 2018年7月6日 下午2:47:31
 */
public class TcpServer implements Runnable{

	private static Logger logger=LoggerFactory.getLogger(TcpServer.class);
	private final MinaServerConfig minaServerConfig;
	private final NioSocketAcceptor acceptor;
	private final IoHandler ioHandler;
	private ProtocolCodecFactory factory;
	//消息处理线程，使用有序线程池，保证所有session 事件与许进行，比如先执行消息执行，在是消息发送，最后关闭事件
	private OrderedThreadPoolExecutor threadPool;
	//过滤器
	private Map<String, IoFilter> filters;
	//服务器是否运行
	protected boolean isRunning =false;
	
	/**
	 * 
	 * @param minaServerConfig 配置
	 * @param ioHandler 消息处理器
	 */
	public TcpServer(MinaServerConfig minaServerConfig,IoHandler ioHandler) {
		this.minaServerConfig=minaServerConfig;
		this.ioHandler=ioHandler;
		acceptor=new NioSocketAcceptor();
	}
	
	public TcpServer(MinaServerConfig minaServerConfig,IoHandler ioHandler,ProtocolCodecFactory factory) {
		this(minaServerConfig,ioHandler);
		this.factory=factory;
	}
	
	/**
	 * 
	 * @param minaServerConfig
	 * @param ioHandler
	 * @param factory
	 * @param filters  不要包含消息解码，线程池过滤器，已默认添加
	 */
	public TcpServer(MinaServerConfig minaServerConfig,IoHandler ioHandler,ProtocolCodecFactory factory,Map<String, IoFilter> filters) {
		this(minaServerConfig, ioHandler,factory);
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
	
	@Override
	public void run() {
		synchronized (this) {
			if(!isRunning) {
				isRunning=true;
				DefaultIoFilterChainBuilder chain=acceptor.getFilterChain();
				if(factory==null) {
					factory=new DefaultProtocolCodecFactory();
				}
				if(factory instanceof DefaultProtocolCodecFactory) {
					ProtocolCodecFactoryImpl defaultFactory=(ProtocolCodecFactoryImpl)factory;
					defaultFactory.getDecoder().setMaxReadSize(minaServerConfig.getMaxReadSize());
					defaultFactory.getEncoder().setMaxScheduledWriteMessages(minaServerConfig.getMaxScheduledWriteMessage());
				}
				
				chain.addLast("codec", new ProtocolCodecFilter(factory));
				threadPool=new OrderedThreadPoolExecutor(minaServerConfig.getOrderedThreadPoolExecutorSize());
				chain.addLast("threadPool", new ExecutorFilter(threadPool));
				if(this.filters!=null) {
					this.filters.forEach((key,filter) ->{
						if(key.equalsIgnoreCase("ssl")||key.equalsIgnoreCase("tls")) {//ssl过滤器必须要添加到首部
							chain.addFirst(key, filter);
						}else {
							chain.addLast(key, filter);
						}
						
					});
				}
				
				acceptor.setReuseAddress(minaServerConfig.isReuseAddress());//允许地址重用
				
				SocketSessionConfig sc=acceptor.getSessionConfig();
				sc.setReuseAddress(minaServerConfig.isReuseAddress());
				sc.setReceiveBufferSize(minaServerConfig.getReceiveBufferSize());
				sc.setSendBufferSize(minaServerConfig.getSendBufferSize());
				sc.setTcpNoDelay(minaServerConfig.isTcpNoDelay());
				sc.setSoLinger(minaServerConfig.getSoLinger());
				sc.setIdleTime(IdleStatus.READER_IDLE, minaServerConfig.getReaderIdleTime());
				sc.setIdleTime(IdleStatus.WRITER_IDLE, minaServerConfig.getWriterIdleTime());
				
				acceptor.setHandler(ioHandler);
				
				try {
					acceptor.bind(new InetSocketAddress(minaServerConfig.getPort()));
					logger.info("已开始监听TCP 端口:{}",minaServerConfig.getPort());
				}catch (Exception e) {
					logger.error("监听TCP端口:{}已被占用",minaServerConfig.getPort());
					logger.error("TCP 服务异常",e);
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
			isRunning=false;
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

	public NioSocketAcceptor getAcceptor() {
		return acceptor;
	}
	
	
}
