package com.jbm.game.engine.mina.service;

import java.util.Map;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoHandler;

import com.jbm.game.engine.mina.MinaTcpClient;
import com.jbm.game.engine.mina.code.ProtocolCodecFactoryImpl;
import com.jbm.game.engine.mina.config.MinaClientConfig;
import com.jbm.game.engine.mina.handler.DefaultClientProtocolHandler;

/**
 * 单个tcp 连接客户端
 * @author JiangBangMing
 *
 * 2018年7月12日 下午1:35:13
 */
public class SingleMinaTcpClientService extends MinaClientService{

	private final MinaTcpClient minaTcpClient;
	
	/**
	 * 
	 * @param minaClientConfig
	 * @param factory
	 * @param ioHandler
	 * @param filters
	 */
	public SingleMinaTcpClientService(MinaClientConfig minaClientConfig,ProtocolCodecFactoryImpl factory,IoHandler ioHandler,Map<String, IoFilter> filters) {
		super(minaClientConfig);
		minaTcpClient=new MinaTcpClient(this, minaClientConfig, ioHandler,factory,filters);
	}
	
	/**
	 * 
	 * @param minaClientConfig
	 * @param factory
	 * @param ioHandler
	 */
	public SingleMinaTcpClientService(MinaClientConfig minaClientConfig,ProtocolCodecFactoryImpl factory,IoHandler ioHandler) {
		super(minaClientConfig);
		minaTcpClient=new MinaTcpClient(this, minaClientConfig, ioHandler,factory);
	}
	
	/**
	 * 
	 * @param minaClientConfig
	 * @param ioHandler
	 */
	public SingleMinaTcpClientService(MinaClientConfig minaClientConfig,IoHandler ioHandler) {
		super(minaClientConfig);
		minaTcpClient=new MinaTcpClient(this, minaClientConfig, ioHandler);
	}

	/**
	 * 
	 * @param minaClientConfig
	 */
	public SingleMinaTcpClientService(MinaClientConfig minaClientConfig) {
		super(minaClientConfig);
		minaTcpClient=new MinaTcpClient(this, minaClientConfig, new DefaultClientProtocolHandler(this));
	}
	
	@Override
	protected void running() {
		minaTcpClient.run();
		
	}
	
	@Override
	public void checkStatus() {
		minaTcpClient.checkStatus();
	}
	public MinaTcpClient getMinaTcpClient() {
		return minaTcpClient;
	}
	
	
}
