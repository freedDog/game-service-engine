package com.jbm.game.engine.mina.service;

import java.util.Map;

import org.apache.mina.core.filterchain.IoFilter;

import com.jbm.game.engine.mina.TcpServer;
import com.jbm.game.engine.mina.code.ClientProtocolCodecFactory;
import com.jbm.game.engine.mina.config.MinaServerConfig;
import com.jbm.game.engine.mina.handler.ClientProtocolHandler;
import com.jbm.game.engine.server.Service;
import com.jbm.game.engine.thread.ServerThread;
import com.jbm.game.engine.thread.ThreadPoolExecutorConfig;
import com.jbm.game.engine.thread.ThreadType;
import com.jbm.game.engine.thread.timer.event.ServerHeartTimer;

/**
 * 游戏前段消息接收 服务
 * @author JiangBangMing
 *
 * 2018年7月9日 下午12:01:34
 */
public class ClientServerService extends Service<MinaServerConfig>{

	protected TcpServer tcpServer;
	protected MinaServerConfig minaServerConfig;
	protected ClientProtocolHandler clientProtocolHandler;
	
	/**
	 * 不创建默认IO线程池
	 * @param minaServerConfig
	 */
	public ClientServerService(MinaServerConfig minaServerConfig) {
		this(null,minaServerConfig);
	}
	
	/**
	 * 使用默认消息处理器
	 * @param threadPoolExecutorConfig
	 * @param minaServerConfig
	 */
	public ClientServerService(ThreadPoolExecutorConfig threadPoolExecutorConfig,MinaServerConfig minaServerConfig) {
		this(threadPoolExecutorConfig,minaServerConfig,new ClientProtocolHandler(8));
	}
	
	/**
	 * 
	 * @param threadPoolExecutorConfig 线程池配置
	 * @param minaServerConfig 服务器配置
	 * @param clientProtocolHandler 消息处理器
	 */
	public ClientServerService(ThreadPoolExecutorConfig threadPoolExecutorConfig,MinaServerConfig minaServerConfig,ClientProtocolHandler clientProtocolHandler) {
		super(threadPoolExecutorConfig);
		this.minaServerConfig=minaServerConfig;
		this.clientProtocolHandler=clientProtocolHandler;
		tcpServer=new TcpServer(minaServerConfig, clientProtocolHandler,new ClientProtocolCodecFactory());
	}
	
	/**
	 * 
	 * @param threadPoolExecutorConfig
	 * @param minaServerConfig
	 * @param clientProtocolHandler
	 * @param filters
	 */
	public ClientServerService(ThreadPoolExecutorConfig threadPoolExecutorConfig,MinaServerConfig minaServerConfig,
			ClientProtocolHandler clientProtocolHandler,Map<String, IoFilter> filters) {
		super(threadPoolExecutorConfig);
		this.minaServerConfig=minaServerConfig;
		this.clientProtocolHandler=clientProtocolHandler;
		tcpServer=new TcpServer(minaServerConfig, clientProtocolHandler,new ClientProtocolCodecFactory(),filters);
	}
	@Override
	protected void running() {
		clientProtocolHandler.setService(this);
		tcpServer.run();
		//添加定时器，如果心跳配置为0，则没有定时器
		ServerThread syncThread=this.getExecutor(ThreadType.SYNC);
		if(syncThread!=null) {
			syncThread.addTimerEvent(new ServerHeartTimer());
		}
	}
	
	@Override
	protected void onShutdown() {
		super.onShutdown();
		tcpServer.stop();
	}

	public TcpServer getTcpServer() {
		return tcpServer;
	}

	public MinaServerConfig getMinaServerConfig() {
		return minaServerConfig;
	}
	
	
	
}
