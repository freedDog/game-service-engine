package com.jbm.game.engine.handler;

import com.google.protobuf.Message;
import com.jbm.game.engine.mina.message.IDMessage;
import com.jbm.game.engine.struct.Person;

/**
 * Tcp 处理器
 * 也可以处理udp请求
 * @author JiangBangMing
 *
 * 2018年7月3日 下午7:07:04
 */
public abstract class TcpHandler  extends AbsHandler{

	private Message message;
	protected long rid;//角色ID
	protected Person person;//角色
	
	@Override
	public Message getMessage() {
		return this.message;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Message> T getMsg() {
		return (T)message;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Person> T getPerson() {
		return (T)person;
	}
	
	@Override
	public void setMessage(Object message) {
		this.message=(Message)message;
	}
	
	public void setPerson(Person person) {
		this.person=person;
	}

	public long getRid() {
		return rid;
	}

	public void setRid(long rid) {
		this.rid = rid;
	}
	
	/**
	 * 发送带ID的消息
	 * @param object
	 */
	public void sendIdMsg(Object object) {
		if(getSession()!=null&&getSession().isConnected()) {
			getSession().write(new IDMessage(session, object, rid));
		}else if(getChannel()!=null&&getChannel().isActive()) {
			getChannel().writeAndFlush(new IDMessage(channel, object, rid,null));
		}
	}
	
}
