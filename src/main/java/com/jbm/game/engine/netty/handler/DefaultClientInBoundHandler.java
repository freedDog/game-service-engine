package com.jbm.game.engine.netty.handler;

import com.jbm.game.engine.netty.service.NettyClientService;
import com.jbm.game.engine.script.ScriptManager;
import com.jbm.game.engine.server.BaseServerConfig;
import com.jbm.game.engine.server.ServerInfo;
import com.jbm.game.engine.server.Service;

import io.netty.channel.ChannelHandlerContext;

/**
 * 内容客户端默认消息
 * @author JiangBangMing
 *
 * 2018年7月13日 下午5:19:49
 */
public class DefaultClientInBoundHandler extends DefaultInBoundHandler{

	private NettyClientService nettyClientService;
	private ServerInfo serverInfo;
	
	public DefaultClientInBoundHandler(NettyClientService nettyClientService,ServerInfo serverInfo) {
		super();
		this.nettyClientService=nettyClientService;
		this.serverInfo=serverInfo;
	}
	
	public DefaultClientInBoundHandler(NettyClientService nettyClientService) {
		super();
		this.nettyClientService=nettyClientService;
	}
	
	@Override
	public Service<? extends BaseServerConfig> getService() {
		return nettyClientService;
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		nettyClientService.channelActive(ctx.channel());
		if(this.serverInfo!=null) {
			serverInfo.onChannelActive(ctx.channel());
		}
		ScriptManager.getInstance().getBaseScriptEntry().executeScripts(IChannelHandlerScript.class,
				script->script.channelActive(DefaultClientInBoundHandler.class, ctx.channel()));
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		nettyClientService.channelInactive(ctx.channel());
	}
	
}
