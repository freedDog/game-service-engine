package com.jbm.game.engine.mina.handler;

import java.util.concurrent.Executor;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.http.HttpRequestImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jbm.game.engine.handler.HandlerEntity;
import com.jbm.game.engine.handler.IHandler;
import com.jbm.game.engine.mina.config.MinaServerConfig;
import com.jbm.game.engine.script.ScriptManager;
import com.jbm.game.engine.server.Service;
import com.jbm.game.engine.util.MsgUtil;

/**
 * http 消息处理器
 * @author JiangBangMing
 *
 * 2018年7月10日 上午10:47:55
 */
public abstract class HttpServerIoHandler implements IoHandler{
	
	private static final Logger logger=LoggerFactory.getLogger(HttpServerIoHandler.class);
	
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		if(!(message instanceof HttpRequestImpl)) {
			logger.warn("HttpServerIoHandler:"+message.getClass().getName());
			return;
		}
		long begin=System.currentTimeMillis();
		
		HttpRequestImpl httpRequest=(HttpRequestImpl)message;
		Class<? extends IHandler> handlerClass=ScriptManager.getInstance().getHttpHandler(httpRequest.getRequestPath());
		HandlerEntity handlerEntity=ScriptManager.getInstance().getHttpHandlerEntity(httpRequest.getRequestPath());
		if(handlerClass==null) {
			handlerClass=ScriptManager.getInstance().getHttpHandler("");
			handlerEntity=ScriptManager.getInstance().getHttpHandlerEntity("");
		}
		
		if(handlerClass==null){
			logger.error("http 容器未能找到 connent = {}的httpMessageBean toString: {}",httpRequest.getRequestPath(),session.getRemoteAddress().toString());
			return;
		}
		try {
			IHandler handler=handlerClass.newInstance();
			handler.setMessage(httpRequest);
			handler.setSession(session);
			handler.setCreateTime(System.currentTimeMillis());
			
			Executor executor=getService().getExecutor(handlerEntity.thread());
			if(executor!=null) {
				executor.execute(handler);
			}else {
				handler.run();
				logger.warn("{} 指定的线程{} 未开启",handlerClass.getName(),handlerEntity.thread().toString());
			}
		}catch (InstantiationException |IllegalAccessException e) {
			logger.error("messageReceived build message error !!!",e);
		}
		
		long cost=System.currentTimeMillis()-begin;
		if(cost>30L) {
			logger.warn(String.format("\t messageReceived %s msgID[%s] builder[%s] cost %d ms", Thread.currentThread().toString(),httpRequest.getRequestPath(),httpRequest.toString(),cost));
		}
		
	}
	
	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		session.closeOnFlush();
	}
	
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		session.closeOnFlush();
		if(logger.isDebugEnabled()) {
			logger.error("exceptionCaught",cause);
		}
		
	}
	
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		if(logger.isDebugEnabled()) {
			logger.debug("http 请求建立"+session);
		}
	}
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		if(logger.isDebugEnabled()) {
			logger.debug("http 请求断开"+session);
		}
	}
	
	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		if(!session.isClosing()) {
			MsgUtil.close(session, "http isClosing");
		}
	}
	
	@Override
	public void inputClosed(IoSession session) throws Exception {
		logger.error("http inputClosed "+session);
		MsgUtil.close(session, "http inputClosed");
	}
	
	
	protected abstract Service<MinaServerConfig> getService();
}
