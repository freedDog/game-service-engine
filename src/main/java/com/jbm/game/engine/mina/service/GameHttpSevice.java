package com.jbm.game.engine.mina.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jbm.game.engine.mina.HttpServer;
import com.jbm.game.engine.mina.config.MinaServerConfig;
import com.jbm.game.engine.mina.handler.HttpServerIoHandler;
import com.jbm.game.engine.server.Service;
import com.jbm.game.engine.thread.ThreadPoolExecutorConfig;

/**
 * 游戏服 http 服务器
 * @author JiangBangMing
 *
 * 2018年7月12日 上午11:25:54
 */
public class GameHttpSevice extends Service<MinaServerConfig>{

	private static final Logger logger=LoggerFactory.getLogger(GameHttpSevice.class);
	
	private final HttpServer httpServer;
	private final MinaServerConfig minaServerConfig;
	
	public GameHttpSevice(ThreadPoolExecutorConfig threadPoolExecutorConfig,MinaServerConfig minaServerConfig) {
		super(threadPoolExecutorConfig);
		this.minaServerConfig=minaServerConfig;
		this.httpServer=new HttpServer(minaServerConfig, new GameHttpServerHandler(this));
	}
	
	public GameHttpSevice(MinaServerConfig minaServerConfig) {
		super(null);
		this.minaServerConfig=minaServerConfig;
		this.httpServer=new HttpServer(minaServerConfig, new GameHttpServerHandler(this));
	}
	
	@Override
	protected void running() {
		if(logger.isDebugEnabled()) {
			logger.debug("run ...");
		}
		httpServer.run();
	}		
	
	@Override
	protected void onShutdown() {
		super.onShutdown();
		httpServer.stop();
		if(logger.isDebugEnabled()) {
			logger.debug("stop ...");
		}
	}
	
	@Override
	public String toString() {
		return minaServerConfig.getName();
	}
	
	
	
	public MinaServerConfig getMinaServerConfig() {
		return minaServerConfig;
	}



	/**
	 *消息处理器
	 * @author JiangBangMing
	 *
	 * 2018年7月12日 上午11:29:04
	 */
	private class GameHttpServerHandler extends HttpServerIoHandler{
		private Service<MinaServerConfig> service;
		
		public GameHttpServerHandler(Service<MinaServerConfig> service) {
			this.service=service;
		}
		@Override
		protected Service<MinaServerConfig> getService() {
			return this.service;
		}
	}
}
