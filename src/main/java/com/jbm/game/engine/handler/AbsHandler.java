package com.jbm.game.engine.handler;

import org.apache.mina.core.session.IoSession;

import io.netty.channel.Channel;

/**
 * 抽象 handler
 * @author JiangBangMing
 *
 * 2018年7月3日 下午5:59:07
 */
public abstract class AbsHandler implements IHandler{

	
	protected IoSession session;
	protected long createTime;
	protected Channel channel;
	
	@Override
	public IoSession getSession() {
		return this.session;
	}
	@Override
	public void setSession(IoSession session) {
		this.session=session;
	}
	@Override
	public void setCreateTime(long time) {
		this.createTime=time;
	}
	
	@Override
	public long getCreateTime() {
		return this.createTime;
	}
	@Override
	public Object getParameter() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setParameter(Object parameter) {
		// TODO Auto-generated method stub
		
	}
	
	public Channel getChannel() {
		return channel;
	}
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
}
