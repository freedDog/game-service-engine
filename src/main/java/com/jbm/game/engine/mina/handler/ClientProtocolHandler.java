package com.jbm.game.engine.mina.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.jbm.game.engine.handler.HandlerEntity;
import com.jbm.game.engine.handler.IHandler;
import com.jbm.game.engine.handler.TcpHandler;
import com.jbm.game.engine.mina.config.MinaServerConfig;
import com.jbm.game.engine.script.ScriptManager;
import com.jbm.game.engine.server.BaseServerConfig;
import com.jbm.game.engine.server.Service;
import com.jbm.game.engine.util.IntUtil;
import com.jbm.game.engine.util.MsgUtil;

/**
 * 游戏前段消息处理器
  * 游戏前端消息处理器
 * <p>
 * 包长度（2）+消息ID（4）+消息长度（4）+消息内容
 * 
 * <br>
 * decoder 已去掉包长度
 * </p>
 * @author JiangBangMing
 *
 * 2018年7月5日 下午9:16:49
 */
public class ClientProtocolHandler extends DefaultProtocolHandler{
	
	private static final Logger logger=LoggerFactory.getLogger(ClientProtocolHandler.class);
	protected Service<MinaServerConfig> service;

	public ClientProtocolHandler(int messageHeaderLength) {
		super(messageHeaderLength);
	}
	
	@Override
	public void messageReceived(IoSession session, Object obj) throws Exception {
		byte[] bytes=(byte[])obj;
		try {
			if(bytes.length<messageHeaderLength) {
				logger.error("messageReceived:消息长丢{} 小于等于消息长度{}",bytes.length,messageHeaderLength);
				return;
			}
			int mid=IntUtil.bigEndianByteToInt(bytes, 0, 4);//消息id
			if(ScriptManager.getInstance().tcpMsgIsRegister(mid)) {
				Class<? extends IHandler> handlerClass=ScriptManager.getInstance().getTcpHandler(mid);
				HandlerEntity handlerEntity=ScriptManager.getInstance().getTcpHandlerEntity(mid);
				if(handlerClass!=null) {
					Message message=MsgUtil.buildMessage(handlerEntity.msg(), bytes,messageHeaderLength,bytes.length-messageHeaderLength);
					TcpHandler handler=(TcpHandler)handlerClass.newInstance();
					if(handler!=null) {
						messageHandler(session, handlerEntity, message, handler);
						return;
					}
				}
			}
			forward(session, mid, bytes);
		}catch (Exception e) {
			logger.error("messageReceived",e);
		}
	}
	@Override
	protected void forward(IoSession session, int msgID, byte[] bytes) {
		logger.warn("消息[{}] 未实现",msgID);
	}
	@Override
	public Service<? extends BaseServerConfig> getService() {
		return service;
	}
	
	public void setService(Service<MinaServerConfig> service) {
		this.service=service;
	}
}
