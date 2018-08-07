package com.jbm.game.engine.thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.simpleframework.xml.Element;

/**
 * 线程池配置
 * @author JiangBangMing
 *
 * 2018年7月9日 下午12:04:16
 */
public class ThreadPoolExecutorConfig {

	//线程池名称
	@Element(required=false)
	private String name;
	
	//核心线程池大小
	@Element(required=true)
	private int corePoolSize=20;
	
	//线程池最大值
	@Element(required=true)
	private int maxPoolSize=200;
	
	//线程池保持活跃时间(秒)
	@Element(required=true)
	private long keepAliveTime=30L;
	
	//心跳间隔(大于0 开启定时监测线程)
	@Element(required=false)
	private int heart=0;
	
	//缓存命令数
	@Element(required=false)
	private int commandSize=100000;
	
	public ThreadPoolExecutor newThreadPoolExecutor() throws RuntimeException{
		return new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<>(commandSize),new IoThreadFactory());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public long getKeepAliveTime() {
		return keepAliveTime;
	}

	public void setKeepAliveTime(long keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}

	public int getHeart() {
		return heart;
	}

	public void setHeart(int heart) {
		this.heart = heart;
	}

	public int getCommandSize() {
		return commandSize;
	}

	public void setCommandSize(int commandSize) {
		this.commandSize = commandSize;
	}
	
	
	
	
}
