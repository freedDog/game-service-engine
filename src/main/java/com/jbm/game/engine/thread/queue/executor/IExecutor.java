package com.jbm.game.engine.thread.queue.executor;

/**
 * 执行器接口定义
 * @author JiangBangMing
 *
 * 2018年7月14日 下午12:59:11
 */
public interface IExecutor<T extends Runnable> {

	
	/**
	 * 执行任务
	 * @param cmdTask
	 */
	public void execute(T cmdTask);
	
	/**
	 * 停止所有线程
	 */
	public void stop();
}
