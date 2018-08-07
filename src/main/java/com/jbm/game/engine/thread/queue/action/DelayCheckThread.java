package com.jbm.game.engine.thread.queue.action;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1:定时监测队列 线程
 * 2:队列中时间到的action 从新加入执行队列
 * @author JiangBangMing
 *
 * 2018年7月14日 下午12:34:14
 */
public class DelayCheckThread extends Thread{

	private static final Logger logger=LoggerFactory.getLogger(DelayCheckThread.class);
	
	/**
	 * 检测相隔时间(单位:毫秒)
	 */
	private static final int FRAME_PER_SECOND=120;
	/**
	 * 线程锁
	 */
	private Object lock=new Object();//线程锁
	
	private List<DelayAction> delayQueue;//延迟队列
	
	private List<DelayAction> execQueue;//执行队列，临时存储
	
	private volatile boolean isRunning;
	
	public DelayCheckThread(String prefix) {
		super(prefix+"-DelayCheckThread");
		delayQueue=new ArrayList<>();
		execQueue=new ArrayList<>();
		isRunning=true;
		setPriority(Thread.MAX_PRIORITY);//给予高优先级
	}
	
	public boolean isRunning() {
		return this.isRunning;
	}
	
	public void stopping() {
		if(isRunning) {
			isRunning=false;
		}
	}
	
	@Override
	public void run() {
		long balance=0;
		while(isRunning) {
			try {
				int execute=0;
				//读取待执行的队列，如果没有可以执行的动作则等待
				poll();
				if(execQueue.size()==0) {
					continue;
				}
				
				long start=System.currentTimeMillis();
				execute=execActions();
				execQueue.clear();
				long end=System.currentTimeMillis();
				long interval=end-start;
				balance+=FRAME_PER_SECOND-interval;
				if(interval>FRAME_PER_SECOND) {
					logger.warn("DelayCheckThread is spent to much time: "+interval+" ms,execute ="+execute);
				}
				if(balance>0) {
					Thread.sleep((int)balance);
					balance=0;
				}else {
					if(balance<-1000) {
						balance+=1000;
					}
				}
				
			}catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	/**
	 * 添加action 到等待队列
	 * @param delayAction
	 */
	public void addDelayAction(DelayAction delayAction) {
		synchronized (lock) {
			delayQueue.add(delayAction);
			lock.notifyAll();
		}
	}
	
	/**
	 * 监测队列是否能执行，返回执行成功的Action 数量
	 * @return
	 */
	private int execActions() {
		int executeCount=0;
		for(DelayAction delayAction:execQueue) {
			try {
				long begin=System.currentTimeMillis();
				if(delayAction==null) {
					logger.warn("acton null");
					continue;
				}
				if(!delayAction.canExec(begin)) {
					addDelayAction(delayAction);//返回给等待队列
				}else {
					delayAction.getActionQueue().enDelayQueue(delayAction);//添加到执行队列
				}
				executeCount++;
				long end=System.currentTimeMillis();
				if(end-begin>FRAME_PER_SECOND) {
					logger.warn(delayAction.toString()+" spent too much time.time : "+(end-begin));
				}
			}catch (Exception e) {
				logger.error("执行action 异常"+delayAction.toString(),e);
			}
		}
		return executeCount;
	}
	
	private void poll() throws InterruptedException{
		synchronized (lock) {
			if(delayQueue.size()==0) {
				/**
				 * 千万注意：
					当在对象上调用wait()方法时，执行该代码的线程立即放弃它在对象上的锁。然而调用notify()时，
					并不意味着这时线程会放弃其锁。如果线程任然在完成同步代码，则线程在移出之前不会放弃锁。
					因此，只要调用notify()并不意味着这时该锁变得可用。
				 */
				lock.wait();
			}else {
				execQueue.addAll(delayQueue);
				delayQueue.clear();
				lock.notifyAll();
			}
		}
	}
}
