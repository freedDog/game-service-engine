package com.jbm.game.engine.thread;

import java.util.concurrent.Executor;

/**
 * 自定义线程池
 * @author JiangBangMing
 *
 * 2018年7月9日 下午2:00:58
 */
public abstract class ExecutorPool implements Executor{
	
	/**
	 * 关闭线程
	 */
	public abstract void stop();
}
