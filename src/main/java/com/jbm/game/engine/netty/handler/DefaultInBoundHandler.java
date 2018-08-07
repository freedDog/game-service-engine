package com.jbm.game.engine.netty.handler;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.jbm.game.engine.handler.HandlerEntity;
import com.jbm.game.engine.handler.IHandler;
import com.jbm.game.engine.handler.TcpHandler;
import com.jbm.game.engine.mina.message.IDMessage;
import com.jbm.game.engine.script.ScriptManager;
import com.jbm.game.engine.server.BaseServerConfig;
import com.jbm.game.engine.server.Service;
import com.jbm.game.engine.util.MsgUtil;
import com.jbm.game.engine.util.TimeUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 默认接收消息处理器<br>
 * 消息直接用netty线程池处理，分发请重新实现messageHandler
 * @author JiangBangMing
 *
 * 2018年7月13日 下午5:09:01
 */
public abstract class DefaultInBoundHandler extends SimpleChannelInboundHandler<IDMessage> {

	private static final Logger logger=LoggerFactory.getLogger(DefaultInBoundHandler.class);
	
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, IDMessage msg) throws Exception {
		if(!ScriptManager.getInstance().tcpMsgIsRegister(msg.getMsgId())) {
			forward(msg);
			return;
		}
		Class<? extends IHandler> handlerClass=ScriptManager.getInstance().getTcpHandler(msg.getMsgId());
		TcpHandler handler=(TcpHandler)handlerClass.newInstance();
		handler.setCreateTime(TimeUtil.currentTimeMillis());
		HandlerEntity handlerEntity=ScriptManager.getInstance().getTcpHandlerEntity(msg.getMsgId());
		Message message=MsgUtil.buildMessage(handlerEntity.msg(), (byte[])msg.getMsg());
		handler.setMessage(message);
		handler.setRid(msg.getUserID());
		handler.setChannel(ctx.channel());
		messagehandler(handler, handlerEntity);
	}
	/**
	 * 消息处理
	 * @param handler
	 * @param handlerEntity
	 */
	protected void messagehandler(TcpHandler handler,HandlerEntity handlerEntity) {
		if(getService()!=null) {
			Executor executor=getService().getExecutor(handlerEntity.thread());
			if(executor!=null) {
				executor.execute(handler);
				return;
			}
		}
		handler.run();
	}
	
	public abstract Service<? extends BaseServerConfig> getService(); 
	
	protected void forward(IDMessage msg) {
		logger.info("消息{} 未实现",msg.getMsgId());
	}
	
}
