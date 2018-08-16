package com.jbm.game.engine.mina.handler;

import java.util.concurrent.Executor;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.jbm.game.engine.handler.HandlerEntity;
import com.jbm.game.engine.handler.IHandler;
import com.jbm.game.engine.handler.TcpHandler;
import com.jbm.game.engine.script.ScriptManager;
import com.jbm.game.engine.server.BaseServerConfig;
import com.jbm.game.engine.server.Service;
import com.jbm.game.engine.util.MsgUtil;

/**
 * 默认消息处理器
 * <p>
 * 消息头+消息内容 <br>
 * 消息头可能有消息长度、消息ID、用户ID
 * @author JiangBangMing
 *
 * 2018年7月5日 下午8:39:56
 */
public abstract class DefaultProtocolHandler implements IoHandler{

	private static final Logger logger=LoggerFactory.getLogger(DefaultProtocolHandler.class);
	protected final int messageHeaderLength;//消息都长度
	
	public DefaultProtocolHandler(int messageHeaderLength) {
		this.messageHeaderLength=messageHeaderLength;
	}
	
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		if(logger.isWarnEnabled()) {
			logger.warn("已经创建连接{}",session);
		}
	}
	
	@Override
	public void sessionOpened(IoSession session){
		if(logger.isWarnEnabled()) {
			logger.warn("已经打开连接{}",session);
		}
	}
	
	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		if(logger.isWarnEnabled()) {
			logger.warn("连接{} 已经关闭sessionClosed",session);
		}
	}
	
	@Override
	public void sessionIdle(IoSession session, IdleStatus idleStatus) throws Exception {
		if(logger.isWarnEnabled()) {
			logger.warn("连接{} 处于空闲{}",session,idleStatus);
		}
	}
	@Override
	public void inputClosed(IoSession session) throws Exception {
		if(logger.isWarnEnabled()) {
			logger.warn("连接{} inputClose已关闭",session);
		}
		session.closeNow();
	}
	
	@Override
	public void exceptionCaught(IoSession session, Throwable throwable) throws Exception {
		logger.error("连接{}异常:{}",session,throwable);
		session.closeNow();
	}
	
	@Override
	public void messageReceived(IoSession session, Object obj) throws Exception {
		byte[] bytes=(byte[])obj;
		try {
			if(bytes.length<messageHeaderLength) {
				logger.error("messageReceived:消息长度{}小于等于消息头长度{}",bytes.length,messageHeaderLength);
				return;
			}
			int offset=messageHeaderLength>4?8:0;
			int msgID=MsgUtil.getMessageID(bytes, offset);//消息id
			
			if(ScriptManager.getInstance().tcpMsgIsRegister(msgID)) {
				Class<? extends IHandler> handlerClass=ScriptManager.getInstance().getTcpHandler(msgID);
				HandlerEntity handlerEntity=ScriptManager.getInstance().getTcpHandlerEntity(msgID);
				if(handlerClass!=null) {
					Message message=MsgUtil.buildMessage(handlerEntity.msg(), bytes,messageHeaderLength,bytes.length-messageHeaderLength);
					TcpHandler handler=(TcpHandler)handlerClass.newInstance();
					if(handler!=null) {
						if(offset>0) {//偏移量大于0，又发送玩家ID
							long rid=MsgUtil.getMessageRID(bytes, 0);
							handler.setRid(rid);
						}
						messageHandler(session, handlerEntity, message, handler);
						return;
					}
				}
			}
			forward(session, msgID, bytes);
		}catch (Exception e) {
			logger.error("messageReceived",e);
			int msgid=MsgUtil.getMessageID(bytes,0);
			logger.error("尝试按0 移位处理,id:{}",msgid);
		}
	}
	
	protected void messageHandler(IoSession session,HandlerEntity handlerEntity,Message message,TcpHandler handler) {
		handler.setMessage(message);
		handler.setSession(session);
		handler.setCreateTime(System.currentTimeMillis());
		Executor executor=getService().getExecutor(handlerEntity.thread());
		if(executor==null) {
//			if(logger.isDebugEnabled()) {
//				logger.debug("处理器{} 没有分配线程",handler.getClass().getName());
//			}
			handler.run();
			return;
		}
		executor.execute(handler);
	}
	
	/**
	 * 转发消息
	 * @param session
	 * @param msgID
	 * @param bytes
	 */
	protected abstract void forward(IoSession session,int msgID,byte[] bytes);
	
	
	public abstract Service<? extends BaseServerConfig> getService();
}
