package com.jbm.game.engine.thread.queue;

import java.util.Queue;

import com.jbm.game.engine.thread.queue.action.Action;
import com.jbm.game.engine.thread.queue.action.DelayAction;

/**
 * action 队列接口
 * @author JiangBangMing
 *
 * 2018年7月13日 下午9:34:50
 */
public interface IActionQueue<T extends Action,E extends DelayAction> {

	/**
	 * 添加延时执行任务
	 * @param delayAction
	 */
	public void enDelayQueue(E delayAction);
	
	/**
	 * 清空队列
	 */
	public void clear();
	
	/**
	 * 获取队列
	 * @return
	 */
	public Queue<T> getActionQueue();
	
	/**
	 * 入队列
	 * @param action
	 */
	public void enqueue(T action);
	
	/**
	 * 出队列
	 * @param action
	 */
	public void dequeue(T action);
}
