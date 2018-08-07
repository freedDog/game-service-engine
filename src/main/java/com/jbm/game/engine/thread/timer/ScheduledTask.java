package com.jbm.game.engine.thread.timer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 独立周期性单线程
 * @author JiangBangMing
 *
 * 2018年7月24日 下午3:39:59
 */
public abstract class ScheduledTask {

	private static final Logger logger=LoggerFactory.getLogger(ScheduledTask.class);
	private final ScheduledExecutorService scheduler;
	private final int period;//周期
	
	/**
	 * 
	 * @param taskMaxTime 执行周期
	 */
	public ScheduledTask(int taskMaxTime) {
		scheduler=Executors.newScheduledThreadPool(1);
		
		Executors.newScheduledThreadPool(1, new ThreadFactory() {
			
			@Override
			public Thread newThread(Runnable r) {
				Thread thread=new Thread(this.getClass().getSimpleName());
				return thread;
			}
		});
		period=taskMaxTime<100?100:taskMaxTime;
	}
	
	/**
	 * 启动线程
	 */
	public void start() {
		scheduler.scheduleAtFixedRate(new Task(), 100, period, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * 关闭调用
	 */
	public void shutdown() {
		scheduler.shutdown();
	}
	
	/**
	 * 任务逻辑
	 */
	protected abstract void executeTask();
	
	public class Task implements Runnable{
		@Override
		public void run() {
			try {
				long begin=System.currentTimeMillis();
				executeTask();
				if(System.currentTimeMillis()-begin>period) {
					if(logger.isDebugEnabled()) {
						logger.debug("定时器{} 执行{} ms",this.getClass().getSimpleName(),System.currentTimeMillis()-begin);
					}
				}
			}catch (Exception e) {
				logger.error("定时任务",e);
			}
			
		}
	}
}
