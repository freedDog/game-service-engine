package com.jbm.game.engine.thread.queue.executor;

import com.jbm.game.engine.thread.queue.action.Action;
import com.jbm.game.engine.thread.queue.action.DelayAction;

/**
 * 带延迟执行的线程执行器接口
 * @author JiangBangMing
 *
 * 2018年7月14日 下午1:00:23
 */
public interface IDelayExecutor extends IExecutor<Action>{

	
	/**
	 * 执行延迟、定时action
	 * @param delayAction
	 */
	public void executeDelayAction(DelayAction delayAction);
}
