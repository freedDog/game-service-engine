package com.jbm.game.engine.thread.queue;

import java.util.Queue;

import com.jbm.game.engine.thread.queue.action.Action;
import com.jbm.game.engine.thread.queue.action.DelayAction;
import com.jbm.game.engine.thread.queue.executor.IDelayExecutor;

/**
 * 可执行的action 队列
 * @author JiangBangMing
 *
 * 2018年7月14日 下午1:53:20
 */
public class ExecutorActionQueue extends ExecutorHandlerQueue<Action> implements IActionQueue<Action, DelayAction>{
	
	public ExecutorActionQueue(IDelayExecutor executor,String queueName) {
		super(executor, queueName);
	}
	
	public void enDelayQueue(DelayAction delayAction) {
		((IDelayExecutor)executor).executeDelayAction(delayAction);
	}
	
	@Override
	public Queue<Action> getActionQueue() {
		return getQueue();
	}
}
