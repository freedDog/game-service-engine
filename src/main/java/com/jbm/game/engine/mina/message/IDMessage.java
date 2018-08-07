package com.jbm.game.engine.mina.message;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import com.jbm.game.engine.util.MsgUtil;

import io.netty.channel.Channel;

/**
  * 带用户ID的消息
 * <br>
 * netty消息发送必须设置msgID,mina不能设置
 * @author JiangBangMing
 *
 * 2018年7月3日 下午7:17:45
 */
public final class IDMessage implements Runnable {
	
	private final Object msg;
	private final long userID;//用户ID或角色ID,当角色ID不存在时，用用户ID
	private IoSession session;
	private Channel channel;
	private Integer msgId;
	
	/**
	 * netty使用
	 * @param channel
	 * @param msg
	 * @param userID
	 * @param msgId
	 */
	public IDMessage(Channel channel,Object msg,long userID,Integer msgId) {
		super();
		this.channel=channel;
		this.msg=msg;
		this.userID=userID;
		this.msgId=msgId;
	}
	/**
	 * mina使用
	 * @param session
	 * @param msg
	 * @param userID
	 */
	public IDMessage(IoSession session,Object msg,long userID) {
		this.msg=msg;
		this.userID=userID;
		this.session=session;
	}
	
	@Override
	public void run() {
		if(session!=null&&session.isConnected()) {
			IoBuffer buf=MsgUtil.toIobuffer(this);
			session.write(buf);
		}else if(channel!=null&&channel.isActive()) {
			channel.write(this);
		}
	}
	public Integer getMsgId() {
		return msgId;
	}
	public void setMsgId(Integer msgId) {
		this.msgId = msgId;
	}
	public Object getMsg() {
		return msg;
	}
	public long getUserID() {
		return userID;
	}
	public IoSession getSession() {
		return session;
	}

	
}
