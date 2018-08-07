package com.jbm.game.engine.mina.service;

import java.util.concurrent.PriorityBlockingQueue;

import org.apache.mina.core.session.IoSession;

import com.jbm.game.engine.mina.config.MinaClientConfig;
import com.jbm.game.engine.mina.config.MinaServerConfig;
import com.jbm.game.engine.server.ITcpClientService;
import com.jbm.game.engine.server.Service;
import com.jbm.game.engine.thread.ThreadPoolExecutorConfig;

/**
 * 内部客户端服务
 * @author JiangBangMing
 *
 * 2018年7月9日 下午2:49:16
 */
public abstract class MinaClientService extends Service<MinaServerConfig> implements ITcpClientService<MinaServerConfig>{

	private MinaClientConfig minaClientConfig;
	
	/**
	 * 连接会话
	 */
	private final PriorityBlockingQueue<IoSession> sessions=new PriorityBlockingQueue<>(128,(IoSession session1,IoSession session2)->{
		int res=session1.getScheduledWriteMessages()-session2.getScheduledWriteMessages();
		if(res==0) {
			res=(int)(session1.getWrittenBytes()-session2.getWrittenBytes());
		}
		return res;
	}); 
	
	/**
	 * 无线程池
	 * @param minaServerConfig
	 */
	public MinaClientService(MinaClientConfig minaClientConfig) {
		this(null,minaClientConfig);
	}
	
	public MinaClientService(ThreadPoolExecutorConfig threadPoolExecutorConfig,MinaClientConfig minaClientConfig) {
		super(threadPoolExecutorConfig);
		this.minaClientConfig=minaClientConfig;
	}
	
	/**
	 * 连接建立
	 * @param session
	 */
	public void onIoSessionConnect(IoSession session) {
		sessions.add(session);
	}
	
	/**
	 * 连接关闭移除
	 * @param session
	 */
	public void onIoSessionClosed(IoSession session) {
		sessions.remove(session);
	}
	
	/**
	 * 是否有连接
	 * @return
	 */
	public boolean isSessionEmpty() {
		return sessions.isEmpty();
	}
	
	/**
	 * 发送消息
	 */
	public boolean sendMsg(Object obj) {
		IoSession session=getMostIdleIoSession();
		if(session!=null) {
			session.write(obj);
			return true;
		}
		return false;
	}
	
	public IoSession getMostIdleIoSession() {
		IoSession session=null;
		while(session==null&&!sessions.isEmpty()) {
			session=sessions.peek();
			if(session!=null&&session.isConnected()) {
				break;
			}else {
				sessions.poll();
			}
		}
		return session;
	}

	public MinaClientConfig getMinaClientConfig() {
		return minaClientConfig;
	}


	
}
