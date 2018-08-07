package com.jbm.game.engine.netty;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jbm.game.engine.netty.code.DefaultClientChannelInitializer;
import com.jbm.game.engine.netty.config.NettyClientConfig;
import com.jbm.game.engine.netty.service.NettyClientService;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * Netty Tcp 客户端
 * @author JiangBangMing
 *
 * 2018年7月13日 下午4:20:40
 */
public class NettyTcpClient implements Runnable{

	private static final Logger logger=LoggerFactory.getLogger(NettyTcpClient.class);
	
	//配置
	private NettyClientConfig nettyClientConfig;
	
	//工作组
	private EventLoopGroup group;
	
	//初始化channel
	private ChannelInitializer<SocketChannel> channelInitializer;
	
	//服务
	private NettyClientService service;
	
	private Bootstrap bootstrap;
	//连接
	private final ChannelGroup channels=new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	
	public NettyTcpClient(NettyClientService nettyClientService) {
		this.nettyClientConfig=nettyClientService.getNettyClientConfig();
		this.channelInitializer=new DefaultClientChannelInitializer(nettyClientService);
	}
	/**
	 * 使用本地默认service 配置
	 * @param nettyClientService
	 * @param channelInitializer
	 */
	public NettyTcpClient(NettyClientService nettyClientService,ChannelInitializer<SocketChannel> channelInitializer) {
		this.nettyClientConfig=nettyClientService.getNettyClientConfig();
		this.service=nettyClientService;
		this.channelInitializer=channelInitializer;
	}
	
	public NettyTcpClient(NettyClientService nettyClientService,ChannelInitializer<SocketChannel> channelInitializer,
			NettyClientConfig nettyClientConfig) {
		this.nettyClientConfig=nettyClientConfig;
		this.service=nettyClientService;
		this.channelInitializer=channelInitializer;
	}
	
	@Override
	public void run() {
		connect();
	}
	
	/**
	 * 连接服务器
	 */
	private synchronized void connect() {
		try {
			if(group==null||bootstrap==null) {
				group=new NioEventLoopGroup(nettyClientConfig.getGroupThreadNum());
				bootstrap=new Bootstrap();
				bootstrap.group(group);
				bootstrap.channel(NioSocketChannel.class);
				bootstrap.option(ChannelOption.TCP_NODELAY, nettyClientConfig.isTcpNoDealy());
				bootstrap.handler(channelInitializer);
			}
			for(int i=channels.size();i<nettyClientConfig.getMaxConnectCount();i++) {
				ChannelFuture channelFuture=bootstrap.connect(nettyClientConfig.getIp(),nettyClientConfig.getPort());
				channelFuture.awaitUninterruptibly(10000);//最多等待10秒，如果服务器一直未开启情况下，防止阻塞当前线程
				channels.add(channelFuture.channel());
				channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
					@Override
					public void operationComplete(Future<? super Void> future) throws Exception {
						if(future.isSuccess()) {
							logger.info("连接[{}] 服务器{}:{} 成功",nettyClientConfig.getType().toString(),
									nettyClientConfig.getIp(),nettyClientConfig.getPort());
							connectFinsh();
							channels.add(channelFuture.channel());
						}else {
							logger.error("连接[{}]服务器{}:{} 失败",nettyClientConfig.getType().toString(),
									nettyClientConfig.getIp(),nettyClientConfig.getPort());
							channels.remove(channelFuture.channel());
						}
					}
					
				});
			}
		}catch (Exception e) {
			logger.error("连接客户端",e);
		}
	}
	
	public void stop() {
		channels.close();
		group.shutdownGracefully();
	}
	
	public NettyClientService getService() {
		return service;
	}
	
	
	/**
	 * 检测tcp 连接状态
	 */
	public void checkStatus() {
		if(this.channels.size()<nettyClientConfig.getMaxConnectCount()&&channelInitializer!=null) {
			connect();
		}
		Iterator<Channel> iterator=this.channels.iterator();
		while(iterator.hasNext()) {
			if(!iterator.next().isActive()) {
				iterator.remove();
			}
		}
	}
	/**
	 * 连接服务器完成
	 */
	protected void connectFinsh() {
		
	}
	public NettyClientConfig getNettyClientConfig() {
		return nettyClientConfig;
	}
	public void setNettyClientConfig(NettyClientConfig nettyClientConfig) {
		this.nettyClientConfig = nettyClientConfig;
	}
	
}
