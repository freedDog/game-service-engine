package com.jbm.game.engine.thread.timer;

/**
 * 定时器，end 时间大于0 表示截止时间到即销毁，loop为-1 标识永久循环
 * @author JiangBangMing
 *
 * 2018年7月9日 下午12:39:21
 */
public abstract class TimerEvent implements Runnable{

	//定时器结束时间
	private long end;
	//定时器循环次数
	private int loop=-1;
	
	public TimerEvent(long end,int loop) {
		this.end=end;
		this.loop=loop;
	}
	
	protected TimerEvent(long end) {
		this(end,-1);
	}
	
	public TimerEvent() {
		
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	public long remain() {
		if(this.end==0||loop<0) {
			return 0;
		}
		return this.end-System.currentTimeMillis();
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public int getLoop() {
		return loop;
	}

	public void setLoop(int loop) {
		this.loop = loop;
	}
	
	
}
