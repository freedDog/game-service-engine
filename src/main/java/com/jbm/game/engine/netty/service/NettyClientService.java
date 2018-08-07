package com.jbm.game.engine.netty.service;

import java.util.concurrent.PriorityBlockingQueue;
import com.jbm.game.engine.netty.config.NettyClientConfig;
import com.jbm.game.engine.server.ITcpClientService;
import com.jbm.game.engine.server.Service;
import com.jbm.game.engine.thread.ThreadPoolExecutorConfig;
import io.netty.channel.Channel;

/**
 * Netty 内部客户端
 * @author JiangBangMing
 *
 * 2018年7月13日 下午4:28:30
 */
public abstract class NettyClientService extends Service<NettyClientConfig> implements ITcpClientService<NettyClientConfig>{

	private NettyClientConfig nettyClientConfig;
	
	//拥有的连接
	private final PriorityBlockingQueue<Channel> channels=new PriorityBlockingQueue<>(64, (c1,c2) ->{
		long res=c1.bytesBeforeUnwritable()-c2.bytesBeforeUnwritable();
		if(res==0) {
			res=c1.bytesBeforeWritable()-c2.bytesBeforeWritable();
		}
		return (int) res;
	});
	
	/**
	 * 不开启线程池
	 * @param nettyClientConfig
	 */
	public NettyClientService(NettyClientConfig nettyClientConfig) {
		this(null,nettyClientConfig);
	}
	
	public NettyClientService(ThreadPoolExecutorConfig threadPoolExecutorConfig,NettyClientConfig nettyClientConfig) {
		super(threadPoolExecutorConfig);
		this.nettyClientConfig=nettyClientConfig;
	}
	/**
	 * 连接创建
	 * @param channel
	 */
	public void channelActive(Channel channel) {
		channels.add(channel);
	}
	/**
	 * 连接断开
	 * @param channel
	 */
	public void channelInactive(Channel channel) {
		channels.remove(channel);
	}
	
	/**
	 * 获取空闲连接
	 * @return
	 */
	public Channel getMostIdleChannel() {
		Channel channel=null;
		while(channel==null&&!channels.isEmpty()) {
			channel=channels.peek();
			if(channel!=null&&channel.isActive()) {
				break;
			}else {
				channels.poll();
			}
		}
		return channel;
	}
	
	/**
	 * 发送消息
	 */
	public boolean sendMsg(Object obj) {
		Channel channel=getMostIdleChannel();
		if(channel!=null) {
			channel.writeAndFlush(obj);
			return true;
		}
		return false;
	}
	
	public NettyClientConfig getNettyClientConfig() {
		return nettyClientConfig;
	}

	public void setNettyClientConfig(NettyClientConfig nettyClientConfig) {
		this.nettyClientConfig = nettyClientConfig;
	}
	
	
	
}
