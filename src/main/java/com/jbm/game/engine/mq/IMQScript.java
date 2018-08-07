package com.jbm.game.engine.mq;

import com.jbm.game.engine.script.IScript;

/**
 * MQ 消息处理脚本
 * @author JiangBangMing
 *
 * 2018年7月12日 下午6:51:59
 */
public interface IMQScript extends IScript{

	/**
	 * MQ消息接收处理
	 * @param msg
	 */
	public default void onMessage(String msg) {
		
	}
}
