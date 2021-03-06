package com.jbm.game.engine.thread;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jbm.game.engine.thread.timer.TimerEvent;
import com.jbm.game.engine.thread.timer.TimerThread;

/**
 * 服务器线程
 * <p>
 * 两类线程模型:<br>
 * 1.为逻辑或接受到的消息预先分配一个线程，所有逻辑放在线程队列中依次执行；{@link ServerThread}<br>
 * 2.为逻辑或消息分配一个队列，再由队列分配线程，依次执行 {@link QueueThreadManager}
 * @author JiangBangMing
 *
 * 2018年7月9日 下午12:33:46
 */
public class ServerThread extends Thread implements Executor{

	private static final Logger logger=LoggerFactory.getLogger(ServerThread.class);
	
	//线程名称
	protected String threadName;
	//线程心跳间隔
	protected final long heart;
	//线程处理命令队列
	protected LinkedBlockingQueue<Runnable> command_queue=new LinkedBlockingQueue<>();
	//是否暂停
	protected boolean stop;
	
	//最后一次执行任务的时间
	protected long lastExecuteTime;
	
	protected TimerThread timer;
	//当前执行的线程
	protected Runnable command;
	
	
	public ServerThread(ThreadGroup group,String threadName,long heart,int commandCount,
			Class<? extends TimerEvent>... classLogNames) {
		super(group, threadName);
		this.threadName=threadName;
		this.heart=heart;
		if(this.heart>0) {
			this.timer=new TimerThread(this, classLogNames);
		}
		
		setUncaughtExceptionHandler((Thread t,Throwable e)->{
			ServerThread.logger.info("ServerThread.setUncaughtExceptionHandler",e);
			if(ServerThread.this.timer!=null) {
				ServerThread.this.timer.stop(true);
			}
			ServerThread.this.command_queue.clear();
		});
		
		command_queue=new LinkedBlockingQueue<>();
	}
	
	public void showStackTrace() {
		StringBuilder sb=new StringBuilder();
		long now=System.currentTimeMillis();
		long procc=now-this.getLastExecuteTime();
		sb.append("线程["+this.getName()+" ]可能已卡死!!!"+procc+" ms,执行任务:"+this.getCommand().getClass().getName());
		try {
			StackTraceElement[] elements=this.getStackTrace();
			for(int i=0;i<elements.length;i++) {
				sb.append("\n     "+elements[i].getClassName()+"."+elements[i].getMethodName()+"("
						+elements[i].getFileName()+":"+elements[i].getLineNumber()+")");
			}
		}catch (Exception e) {
			sb.append(e);
		}
		logger.warn(sb.toString());
	}

	@Override
	public void run() {
		if(this.heart>0&&this.timer!=null) {
			this.timer.start();
		}
		this.stop=false;
		int loop=0;
		while(!this.stop&&!isInterrupted()) {
			command=this.command_queue.poll();
			if(command==null) {
				try {
					synchronized (this) {
						loop=0;
						this.lastExecuteTime=0;
						wait();
					}
				}catch (Exception e) {
					logger.error("ServerThread.run 1",e);
				}
			}else {
				try {
					loop++;
					lastExecuteTime=System.currentTimeMillis();
					command.run();
					long cost=System.currentTimeMillis()-lastExecuteTime;
					if(cost>30L) {
						logger.warn("线程：{} 执行 {}消耗时间过长{} 毫秒，当前命令数{} 条",threadName,command.getClass().getName(),cost,command_queue.size());
					}
					if(loop>300) {
						loop=0;
						logger.warn("线程: {} 已循环执行{} 次,当前命令数{} 条",threadName,loop,command_queue.size());
					}
				}catch (Exception e) {
					logger.error("ServerThread["+threadName+"] 执行任务错误",e);
				}
			}
		}
	}
	
	
	public void stop(boolean flag) {
		this.stop=flag;
		logger.info("线程{}停止",this.threadName);
		if(this.timer!=null) {
			this.timer.stop(flag);
		}
		this.command_queue.clear();
		try {
			synchronized (this) {
				notify();
				interrupt();
			}
		}catch (Exception e) {
			logger.error("Main thread "+this.threadName+" notify exception:"+e.getMessage());
		}
	}
	
	public void execute(Runnable command,boolean checkOnly) {
		try {
			if(checkOnly&&this.command_queue.contains(command)) {
				return;
			}
			this.command_queue.add(command);
			synchronized (this) {
				notify();
			}
		}catch (Exception e) {
			logger.error("Main thread "+this.threadName+" notify exception： "+e.getMessage());
		}
	}
	
	@Override
	public void execute(Runnable command) {
		try {
			if(this.command_queue.contains(command)) {
				return;
			}
			this.command_queue.add(command);
			synchronized (this) {
				notify();
			}
		}catch (Exception e) {
			logger.error("Main thread "+this.threadName+" notify exception: "+e.getMessage());
		}
	}
	public TimerThread getTimer() {
		return this.timer;
	}
	
	public void addTimerEvent(TimerEvent event) {
		if(this.timer!=null) {
			this.timer.addTimerEvent(event);
		}
	}
	
	public void removeTimerEvent(TimerEvent event) {
		if(this.timer!=null) {
			this.timer.removeTimerEvent(event);
		}
	}
	
	public boolean contains(Runnable runnable) {
		return this.command_queue.contains(runnable);
	}
	public String getThreadName() {
		return threadName;
	}

	public long getHeart() {
		return heart;
	}

	public long getLastExecuteTime() {
		return lastExecuteTime;
	}


	public Runnable getCommand() {
		return command;
	}
	
	public LinkedBlockingQueue<Runnable> getCommands(){
		return command_queue;
	}
}
