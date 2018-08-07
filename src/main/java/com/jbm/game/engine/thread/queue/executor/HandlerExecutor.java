package com.jbm.game.engine.thread.queue.executor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Handler 任务执行器<执行工厂><br>
 * 默认未使用改套方案<br>
 * 1: 线程池统一分配线程执行任务
 * 2: 统一回收
 * @author JiangBangMing
 *
 * 2018年7月14日 下午1:11:45
 */
public class HandlerExecutor<T extends Runnable> implements IExecutor<T>{
	
	private ThreadPoolExecutor pool;
	
	
	public HandlerExecutor(int corePoolSize,int maxPoolSize,int keepAliveTime,int cacheSize,String prefix) {
		TimeUnit unit=TimeUnit.MINUTES;
		/**
		 * 任务队列
		 */
		
		LinkedBlockingQueue<Runnable> workQueue=new LinkedBlockingQueue<>();
		/**
		 * 队列满到无法接受新任务时，直接抛弃
		 */
		RejectedExecutionHandler handler=new ThreadPoolExecutor.DiscardPolicy();
		if(prefix==null) {
			prefix="";
		}
		
		ThreadFactory threadFactory=new HandlerThreadFactory(prefix);
		pool=new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue,threadFactory,handler);
		
	}
	
	/**
	 * 停止所有线程
	 */
	public void stop() {
		if(!pool.isShutdown()) {
			pool.shutdown();
		}
	}
	/**
	 * 执行
	 */
	@Override
	public void execute(T cmdTask) {
		pool.execute(cmdTask);
	}
	
	/**
	 * 线程工厂
	 * 定义线程创建方式
	 * @author JiangBangMing
	 *
	 * 2018年7月14日 下午1:32:09
	 */
	private	static class HandlerThreadFactory implements ThreadFactory{
		/**
		 * 线程池编号
		 */
		private static final AtomicInteger poolNumber=new AtomicInteger(1);
		/**
		 * 线程组
		 */
		private final ThreadGroup group;
		/**
		 * 线程数编号
		 */
		private final AtomicInteger threadNumber=new AtomicInteger(1);
		/**
		 * 线程组前缀
		 */
		private String namePrefix;
		
		/**
		 * 创建线程接口实现
		 * 线程非守护线程
		 * 线程优先级为：Thread.NORM_PRIORITY
		 */
		public Thread newThread(Runnable runnable) {
			Thread thread=new Thread(group,runnable,(new StringBuilder()).append(namePrefix).append(threadNumber.getAndIncrement()).toString(),0L);
			if(thread.isDaemon()) {
				thread.setDaemon(false);
			}
			if(thread.getPriority()!=Thread.NORM_PRIORITY) {
				thread.setPriority(Thread.NORM_PRIORITY);
			}
			return thread;
		}
		public HandlerThreadFactory(String prefix) {
			SecurityManager securityManager=System.getSecurityManager();
			group=securityManager==null?Thread.currentThread().getThreadGroup():securityManager.getThreadGroup();
			namePrefix=(new StringBuilder()).append("pool-").append(poolNumber.getAndIncrement()).append("-").append(prefix).append("-thread-").toString();
		}
	}
}
