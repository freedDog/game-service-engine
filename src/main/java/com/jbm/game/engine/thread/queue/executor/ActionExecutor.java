package com.jbm.game.engine.thread.queue.executor;

import com.jbm.game.engine.thread.queue.action.Action;
import com.jbm.game.engine.thread.queue.action.DelayAction;
import com.jbm.game.engine.thread.queue.action.DelayCheckThread;

/**
 * 执行action队列的线程池<br>
 * 延迟执行的action，先放置到delay action队列中，延迟时间到了后再加入执行队列
 * @author JiangBangMing
 *
 * 2018年7月14日 下午1:32:47
 */
public class ActionExecutor extends HandlerExecutor<Action> implements IDelayExecutor{

	/**
	 * 延迟、定时 检测线程
	 */
	private DelayCheckThread delayCheckThread;
	
	public ActionExecutor(int corePoolSize,int maxPoolSize,int keepAliveTime,int cacheSize,String prefix) {
		super(corePoolSize, maxPoolSize, keepAliveTime, cacheSize, prefix);
		delayCheckThread=new DelayCheckThread(prefix);
		delayCheckThread.start();
	}
	/**
	 * 执行延迟、定时action
	 */
	public void executeDelayAction(DelayAction delayAction) {
		delayCheckThread.addDelayAction(delayAction);
	}
}
