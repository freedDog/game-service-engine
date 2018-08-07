package com.jbm.game.engine.thread.queue.action;

import com.jbm.game.engine.thread.queue.IActionQueue;

/**
 * 无限循环
 * @author JiangBangMing
 *
 * 2018年7月14日 下午12:31:13
 */
public abstract class InfiniteLoopAction extends DelayAction{

	public InfiniteLoopAction(IActionQueue<Action, DelayAction> queue,int delay) {
		super(queue, delay);
	}
	
	@Override
	public void execute() {
		try {
			loopExecte();
		}catch (Exception e) {
			throw e;
		}finally {
			this.execTime=System.currentTimeMillis()+this.delay;
			getActionQueue().enDelayQueue(this);
		}
	}
	/**
	 * 循环执行接口
	 */
	public abstract void loopExecte();
}
