package com.jbm.game.engine.netty.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.jbm.game.engine.mina.message.IDMessage;
import com.jbm.game.engine.netty.NettyMutilTcpClient;
import com.jbm.game.engine.netty.code.DefaultClientChannelInitializer;
import com.jbm.game.engine.netty.code.DefaultMessageCodec;
import com.jbm.game.engine.netty.config.NettyClientConfig;
import com.jbm.game.engine.netty.handler.DefaultClientInBoundHandler;
import com.jbm.game.engine.netty.handler.DefaultOutBoundHandler;
import com.jbm.game.engine.server.IMutilTcpClientService;
import com.jbm.game.engine.server.ServerInfo;
import com.jbm.game.engine.server.ServerType;
import com.jbm.game.engine.thread.ThreadPoolExecutorConfig;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * netty 多客户端连接服务,连接多个服务器
 * * <p>
 * 一般用于子游戏服务器和网关服，所有玩家共享连接
 * </p>
 * 
 * @author JiangBangMing
 *
 * 2018年7月13日 下午6:37:01
 */
public class MutilNettyTcpClientService extends NettyClientService implements IMutilTcpClientService<NettyClientConfig>{

	protected NettyMutilTcpClient mutilTcpClient=new NettyMutilTcpClient();
	//网关服务器
	protected Map<Integer, ServerInfo> serverMap=new ConcurrentHashMap<>();
	
	
	public MutilNettyTcpClientService(NettyClientConfig nettyClientConfig) {
		super(nettyClientConfig);
	}
	
	public MutilNettyTcpClientService(ThreadPoolExecutorConfig threadPoolExecutorConfig,NettyClientConfig nettyClientConfig) {
		super(threadPoolExecutorConfig,nettyClientConfig);
	}
	
	@Override
	protected void running() {
		
	}
	
	/**
	 * 移除客户端
	 */
	public void removeTcpClient(int serverId) {
		mutilTcpClient.removeTcpClient(serverId);
		serverMap.remove(serverId);
	}
	
	/**
	 * 添加连接服务器
	 * @param port 端口
	 */
	public void addTcpClient(ServerInfo serverInfo,int port) {
		addTcpClient(serverInfo, port, new MutilNettyClientChannelInitializer(this, serverInfo));
	}
	
	/**
	 * 
	 * @param serverInfo
	 * @param port
	 * @param channelInitializer
	 */
	public void addTcpClient(ServerInfo serverInfo,int port,ChannelInitializer<SocketChannel> channelInitializer) {
		if(mutilTcpClient.containsKey(serverInfo.getId())) {
			return;
		}
		NettyClientConfig nettyClientConfig=createNettyClientConfig(serverInfo, port);
		mutilTcpClient.addTcpClient(this, nettyClientConfig,channelInitializer);
	}
	
	public Map<Integer, ServerInfo> getServers(){
		return serverMap;
	}
	
	/**
	 * 监测连接状态
	 */
	public void checkStatus() {
		mutilTcpClient.getTcpClients().values().forEach(  cl ->cl.checkStatus());
	}
	
	/**
	 * 广播所有服务器消息：注意，这里并不是向每个session广播，因为有可能有多个连接到同一个服务器
	 */
	public boolean broadcastMsg(Object obj) {
		if(mutilTcpClient==null) {
			return false;
		}
		IDMessage idm=new IDMessage(null, obj, 0);
		serverMap.values().forEach(server ->{
			server.sendMsg(obj);
		});
		return true;
	}
	
	/**
	 * 发送消息
	 * @param serverId 目标服务器id
	 */
	public boolean sendMsg(Integer serverId,Object msg) {
		if(mutilTcpClient==null) {
			return false;
		}
		IDMessage idm=new IDMessage(null, msg, 0);
		return mutilTcpClient.sendMsg(serverId, idm);
	}
	
	
	/**
	 * 创建连接大厅配置文件
	 * @param serverInfo
	 * @param port
	 * @return
	 */
	private NettyClientConfig createNettyClientConfig(ServerInfo serverInfo,int port) {
		NettyClientConfig config=new NettyClientConfig();
		config.setType(ServerType.GATE);
		config.setId(serverInfo.getId());
		config.setMaxConnectCount(this.getNettyClientConfig().getMaxConnectCount());
		config.setIp(serverInfo.getIp());
		config.setPort(port);
		config.setGroupThreadNum(this.getNettyClientConfig().getGroupThreadNum());
		return config;
	}
	
	/**
	 * 多客户端连接初始化<br>
	 * 消息为12
	 * @author JiangBangMing
	 *
	 * 2018年7月13日 下午7:35:21
	 */
	public class MutilNettyClientChannelInitializer extends DefaultClientChannelInitializer{
		
		public MutilNettyClientChannelInitializer(NettyClientService nettyClientService,ServerInfo serverInfo) {
			super(nettyClientService,serverInfo);
		}
		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ch.pipeline().addLast(new DefaultOutBoundHandler());
			ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(50*1024, 0, 4));//消息包格式：长度（4）+角色ID(8)+消息ID(4)+内容
			ch.pipeline().addLast(new DefaultMessageCodec(12));//消息加解密
			ch.pipeline().addLast(new DefaultClientInBoundHandler(nettyClientService));//消息处理器
		}
		
	}
}
