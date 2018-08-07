package com.jbm.game.engine.mina.handler;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jbm.game.engine.mina.message.MassMessage;
import com.jbm.game.engine.server.BaseServerConfig;
import com.jbm.game.engine.server.Service;
import com.jbm.game.engine.util.MsgUtil;

/**
 * 群发消息处理
 * @author JiangBangMing
 *
 * 2018年7月10日 上午11:18:07
 */
public abstract class MassProtocolHandler implements IoHandler{
	private static final Logger logger=LoggerFactory.getLogger(MassProtocolHandler.class);
	protected final int messageHeaderLength;
	
	public MassProtocolHandler() {
		this.messageHeaderLength=4;
	}
	
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		if(logger.isDebugEnabled()) {
			logger.debug("已创建连接{}",session);
		}
	}
	
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		if(logger.isDebugEnabled()) {
			logger.debug("已打开连接{}",session);
		}
	}
	
	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		
	}
	
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		if(logger.isDebugEnabled()) {
			logger.debug("连接{} 已关闭 sesssionClosed",session);
		}
	}
	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		if(logger.isDebugEnabled()) {
			logger.debug("连接{}处于空闲{}",session,status);
		}
	}
	
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		logger.error("连接{} 异常:{}",session,cause);
		MsgUtil.close(session, "发生错误");
	}
	@Override
	public void inputClosed(IoSession session) throws Exception {
		if(logger.isDebugEnabled()) {
			logger.debug("连接{} inputClose 已关闭",session);
		}
		MsgUtil.close(session, "http inputClosed");
	}
	
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		byte[] bytes=(byte[])message;
		try {
			if(bytes.length<messageHeaderLength) {
				logger.error("messageReceived:消息长度{} 小于等于消息长度{}",bytes.length,messageHeaderLength);
				return;
			}
			IoBuffer buff=IoBuffer.wrap(bytes);
			int buffLength=buff.getInt();
			if(buffLength<=buff.remaining()-8) {//至少有一个id
				byte[] byteBuff=Arrays.copyOfRange(bytes, 4, buffLength+4);
				IoBuffer msg=IoBuffer.wrap(byteBuff);
				ArrayList<Long> targets=new ArrayList<>();
				buff.position(buffLength+4);
				while(buff.remaining()>=8) {
					targets.add(buff.getLong());
				}
				MassMessage mass=new MassMessage(msg, targets);
				messageHandler(session, mass);
			}
		}catch (Exception e) {
			logger.error("messageReceived",e);
		}
	}
	/**
	 * 消息处理
	 * @param session
	 * @param msg
	 */
	protected abstract void messageHandler(IoSession session,MassMessage msg);
	
	protected abstract Service<? extends BaseServerConfig> getService();
}
