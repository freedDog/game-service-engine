package com.jbm.game.engine.thread.queue.action;

import com.jbm.game.engine.thread.queue.IActionQueue;

/**
 * 循环多次执行 action
 * @author JiangBangMing
 *
 * 2018年7月14日 下午12:24:43
 */
public abstract class LoopAction extends DelayAction{

	/**
	 * 计数
	 */
	private int count;
	
	/**
	 * 延迟时间
	 */
	private int delay;
	
	/**
	 * 
	 * @param queue 所属队列
	 * @param delay 间隔多长时间执行一次(第一执行会在首次延迟时到的时候执行
	 * @param count 执行次数
	 */
	public LoopAction(IActionQueue<Action, DelayAction> queue,int delay,int count) {
		super(queue, delay);
		this.count=count;
		this.delay=delay;
	}
	
	public LoopAction(IActionQueue<Action, DelayAction> queue,long startTime,int delay,int count) {
		super(queue, startTime,delay);
		this.count=count;
		this.delay=delay;
	}
	
	@Override
	public void execute() {
		if(count<=0) {
			return ;
		}
		count--;
		loopExecute();
		this.execTime=System.currentTimeMillis()+this.delay;
		getActionQueue().enDelayQueue(this);
	}
	/**
	 * 循环执行接口
	 */
	public abstract void loopExecute();
}
