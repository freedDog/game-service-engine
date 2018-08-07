package com.jbm.game.engine.thread;

/**
 * 线程类型
 * @author JiangBangMing
 *
 * 2018年7月3日 下午7:03:28
 */
public enum ThreadType {
	/**耗时的线程池*/
	IO,
	/**全局同步线程*/
	SYNC,
	/**房间*/
	ROOM
	;
}
