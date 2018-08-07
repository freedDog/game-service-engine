package com.jbm.game.engine.thread.queue.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jbm.game.engine.thread.queue.IActionQueue;

/**
 * action 抽象类
 * @author JiangBangMing
 *
 * 2018年7月13日 下午9:29:28
 */
public abstract class Action implements Runnable{

	private static final Logger logger=LoggerFactory.getLogger(Action.class);
	/**
	 * 队列
	 */
	private IActionQueue<Action, DelayAction> queue;
	
	/**
	 * 创建时间
	 */
	protected long createTime;
	
	
	public Action(IActionQueue<Action, DelayAction> queue) {
		this.queue=queue;
		createTime=System.currentTimeMillis();
	}
	
	public IActionQueue<Action, DelayAction> getActionQueue(){
		return queue;
	}
	
	@Override
	public void run() {
		if(queue!=null) {
			long start=System.currentTimeMillis();
			try {
				execute();
				long end=System.currentTimeMillis();
				long interval=end-start;
				long leftTime=end-createTime;
				if(interval>=1000) {
					if(logger.isWarnEnabled()) {
						logger.warn("execute action :"+this.toString()+",interval: "+interval+",leftTime:"+leftTime+",size :"+queue.getActionQueue().size());	
					}
				}
			}catch (Exception e) {
				logger.error("run action execute exception . action : "+this.toString(),e);
			}finally {
				queue.dequeue(this);
			}
		}
	}
	/**
	 * 执行体
	 */
	public abstract void execute();
}
