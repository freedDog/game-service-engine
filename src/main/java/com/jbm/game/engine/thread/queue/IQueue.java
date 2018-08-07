package com.jbm.game.engine.thread.queue;

import java.util.Queue;

/**
 * 队列接口
 * @author JiangBangMing
 *
 * 2018年7月13日 下午9:31:12
 */
public interface IQueue<T> {

	/**
	 * 清空队列
	 */
	public void clear();
	
	/**
	 * 获取队列
	 * @return
	 */
	public Queue<T> getQueue();
	
	/**
	 * 入队列
	 * @param cmd
	 */
	public void enqueue(T cmd);

	/**
	 * 出队列
	 * @param cmd
	 */
	public void dequeue(T cmd);
}
