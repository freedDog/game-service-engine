package com.jbm.game.engine.redis;

import com.jbm.game.engine.redis.jedis.JedisPubSubMessage;
import com.jbm.game.engine.script.IScript;

/**
 * 订阅消息处理器
 * @author JiangBangMing
 *
 * 2018年7月12日 下午7:27:40
 */
public interface IPubSubScript extends IScript{

	/**
	 * 消息处理
	 * @param channle
	 * @param message
	 */
	public void onMessage(String channle,JedisPubSubMessage message);
}
