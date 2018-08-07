package com.jbm.game.engine.script;

import java.time.LocalTime;

/**
 * 心跳脚本，最低按秒循环
 * @author JiangBangMing
 *
 * 2018年7月4日 下午2:52:59
 */
public interface ITimerEventScript extends IScript{
	
	/**
	 * 每秒执行
	 * @param localTime
	 */
	default void secondHandler(LocalTime localTime) {
		
	}
	/**
	 * 每分钟执行
	 * @param localTime
	 */
	default void minuteHandler(LocalTime localTime) {
		
	}
	/**
	 * 每小时执行
	 * @param localTime
	 */
	default void hourHandler(LocalTime localTime) {
		
	}
	/**
	 * 每天执行
	 * @param localTime
	 */
	default void dayHandler(LocalTime localTime) {
		
	}
}
