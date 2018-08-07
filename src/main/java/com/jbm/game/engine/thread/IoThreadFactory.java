package com.jbm.game.engine.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * IO线程池工厂
 * @author JiangBangMing
 *
 * 2018年7月9日 下午12:11:19
 */
public class IoThreadFactory implements ThreadFactory{
	
	private static final AtomicInteger threadId=new AtomicInteger(1);
	private final ThreadGroup group;
	
	public IoThreadFactory() {
		SecurityManager s=System.getSecurityManager();
		group=(s!=null)?s.getThreadGroup():Thread.currentThread().getThreadGroup();
	}
	
	@Override
	public Thread newThread(Runnable r) {
		Thread thread=new Thread(group,r,ThreadType.IO.toString()+"-"+threadId.getAndIncrement(),0);
		if(thread.isDaemon()) {
			thread.setDaemon(false);
		}
		if(thread.getPriority()!=Thread.NORM_PRIORITY) {
			thread.setPriority(Thread.NORM_PRIORITY);
		}
		return thread;
	}
}
