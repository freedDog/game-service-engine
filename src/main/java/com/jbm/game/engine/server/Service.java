package com.jbm.game.engine.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jbm.game.engine.thread.ExecutorPool;
import com.jbm.game.engine.thread.ServerThread;
import com.jbm.game.engine.thread.ThreadPoolExecutorConfig;
import com.jbm.game.engine.thread.ThreadType;

/**
 * 抽象服务
 * @author JiangBangMing
 *
 * 2018年7月5日 下午9:06:52
 */
public abstract class Service <Conf extends BaseServerConfig> implements Runnable{
	
	private static final Logger logger=LoggerFactory.getLogger(Service.class);
	
	private final Map<ThreadType, Executor> serverThreads=new ConcurrentHashMap<ThreadType, Executor>();
	
	/**
	 * 不创建地图线程组
	 * @param threadPoolExecutorConfig
	 */
	public Service(ThreadPoolExecutorConfig threadPoolExecutorConfig) {
		//初始化
		if(threadPoolExecutorConfig!=null) {
			//io默认线程池，客户端的请求，默认使用执行
			ThreadPoolExecutor ioHandlerThreadExcutor=threadPoolExecutorConfig.newThreadPoolExecutor();
			serverThreads.put(ThreadType.IO, ioHandlerThreadExcutor);
			
			//全局sync线程
			@SuppressWarnings("unchecked")
			ServerThread syncThread=new ServerThread(new ThreadGroup("全局同步线程"), "全局同步线程"+this.getClass().getSimpleName(), threadPoolExecutorConfig.getHeart()
					, threadPoolExecutorConfig.getCommandSize());
			syncThread.start();
			serverThreads.put(ThreadType.SYNC, syncThread);
		}
	}
	
	@Override
	public void run() {
		Runtime.getRuntime().addShutdownHook(new Thread(new CloseByExit(this)));
		initThread();
		running();
	}
	
	/**
	 * 初始化线程
	 */
	protected void initThread() {
		
	}
	
	/**
	 * 运行中
	 */
	protected abstract void  running();
	
	/**
	 * 关闭回调
	 */
	protected void onShutdown() {
		serverThreads.values().forEach(executor ->{
			if(executor!=null) {
				try {
					if(executor instanceof ServerThread) {
						if(((ServerThread)executor).isAlive()) {
							((ServerThread)executor).stop(true);
						}
					}else if(executor instanceof ThreadPoolExecutor) {
						if(!((ThreadPoolExecutor)executor).isShutdown()) {
							((ThreadPoolExecutor)executor).shutdown();
							while (!((ThreadPoolExecutor)executor).awaitTermination(5, TimeUnit.SECONDS)) {
								logger.error("线程池剩余线程:"+((ThreadPoolExecutor)executor).getActiveCount());
							}
						}
					}else if(executor instanceof ExecutorPool) {
						((ExecutorPool)executor).stop();
					}
				}catch (Exception e) {
					logger.error("关闭线程",e);
				}
			}
		});
	}
	
	/**
	 * 关闭
	 * @param flag
	 */
	public void stop(boolean flag) {
		onShutdown();
	}
	
	public Map<ThreadType, Executor> getServerThreads() {
		return serverThreads;
	}
	
	/**
	 * 获得线程
	 * @param threadType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Executor> T getExecutor(ThreadType threadType) {
		return (T)serverThreads.get(threadType);
	}
	
	
	private static final class CloseByExit implements Runnable{
		private final static Logger logger=LoggerFactory.getLogger(CloseByExit.class);
		
		@SuppressWarnings("rawtypes")
		private final Service server;
		
		@SuppressWarnings({ "rawtypes", "unused" })
		public CloseByExit(Service server) {
			this.server=server;
		}
		
		@Override
		public void run() {
			server.onShutdown();
			logger.info("服务器{} 已经停止",server.getClass().getName());
		}
	}
}
		
