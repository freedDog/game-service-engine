package com.jbm.game.engine.netty.handler;

import com.jbm.game.engine.script.IScript;
import com.jbm.game.engine.server.BaseServerConfig;
import com.jbm.game.engine.server.Service;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;

/**
 * netty handler 脚本
 * @author JiangBangMing
 *
 * 2018年7月13日 下午4:32:07
 */
public interface IChannelHandlerScript extends IScript{

	
	/**
	 * channel 激活
	 * @param handlerClass
	 * @param channel
	 */
	public default void channelActive(Class<? extends ChannelHandler> handlerClass,Channel channel) {
		
	}
	
	/***
	 * 激活
	 * @param handlerClass
	 * @param service
	 * @param channel
	 */
	public default void channelActive(Class<? extends ChannelHandler> handlerClass,Service<? extends BaseServerConfig> service,Channel channel) {
		
	}
	
	
	/**
	 * channel 空闲
	 * @param handlerClass
	 * @param channel
	 */
	public default void channelInActive(Class<? extends ChannelHandler> handlerClass,Channel channel) {
		
	}
}
