package com.jbm.game.engine.mina.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jbm.game.engine.mina.service.MinaClientService;
import com.jbm.game.engine.server.BaseServerConfig;
import com.jbm.game.engine.server.Service;
import com.jbm.game.engine.util.IntUtil;

/**
 * 默认内部客户端消息处理器
 * @author JiangBangMing
 *
 * 2018年7月5日 下午9:38:22
 */
public class DefaultClientProtocolHandler extends DefaultProtocolHandler{
	
	private static final Logger logger=LoggerFactory.getLogger(DefaultClientProtocolHandler.class);

	private MinaClientService service;
	
	public DefaultClientProtocolHandler(MinaClientService service) {
		super(4);
		this.service=service;
	}
	
	public DefaultClientProtocolHandler(int messageHeaderLength,MinaClientService service) {
		super(messageHeaderLength);
		this.service=service;
	}
	
	@Override
	public void sessionOpened(IoSession session) {
		super.sessionOpened(session);
		getService().onIoSessionConnect(session);
	}
	
	@Override
	protected void forward(IoSession session, int msgID, byte[] bytes) {
		logger.warn("无法找到消息处理器:msgID{}，bytes{}",msgID,IntUtil.BytesToStr(bytes));
	}
	
	@Override
	public MinaClientService getService() {
		return this.service;
	}
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		super.sessionClosed(session);
		getService().onIoSessionClosed(session);
	}
}
