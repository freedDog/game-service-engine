package com.jbm.game.engine.handler;

import org.apache.mina.core.session.IoSession;

/**
 * 处理器接口
 * @author JiangBangMing
 *
 * 2018年7月3日 下午5:54:48
 */
public interface IHandler extends Runnable{

	/**
	 * 会话
	 * @return
	 */
	IoSession getSession();
	/**
	 * 设置会话
	 * @param session
	 */
	void setSession(IoSession session);
	
	/**
	 * 消息
	 * @return
	 */
	Object getMessage();
	
	/**
	 * 消息
	 * @param message
	 */
	void setMessage(Object message);

	/**
	 *创建时间
	 * @return
	 */
	long getCreateTime();
	
	/**
	 * 创建时间
	 * @param time
	 */
	void setCreateTime(long time);
	
	/**
	 * http参数
	 * @return
	 */
	Object getParameter();
	
	/**
	 * http参数
	 * @param parameter
	 */
	void setParameter(Object parameter);
	
}
