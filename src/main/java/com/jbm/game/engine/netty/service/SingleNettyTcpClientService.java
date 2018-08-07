package com.jbm.game.engine.netty.service;

import com.jbm.game.engine.netty.NettyTcpClient;
import com.jbm.game.engine.netty.config.NettyClientConfig;

/**
 * netty 单连接客户端
 * @author JiangBangMing
 *
 * 2018年7月13日 下午5:39:00
 */
public class SingleNettyTcpClientService extends NettyClientService{

	private final NettyTcpClient nettyTcpClient;
	
	public SingleNettyTcpClientService(NettyClientConfig nettyClientConfig) {
		super(nettyClientConfig);
		nettyTcpClient=new NettyTcpClient(this);
	}
	
	@Override
	protected void running() {
		nettyTcpClient.run();
	}
	@Override
	public void checkStatus() {
		nettyTcpClient.checkStatus();
	}
	public NettyTcpClient getNettyTcpClient() {
		return nettyTcpClient;
	}
	
	
}
