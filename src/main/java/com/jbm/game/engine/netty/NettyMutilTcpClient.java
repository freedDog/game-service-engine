package com.jbm.game.engine.netty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jbm.game.engine.netty.code.DefaultClientChannelInitializer;
import com.jbm.game.engine.netty.config.NettyClientConfig;
import com.jbm.game.engine.netty.service.NettyClientService;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * netty 多个连接tcp,连接多个服务器
 * @author JiangBangMing
 *
 * 2018年7月13日 下午6:39:04
 */
public class NettyMutilTcpClient {

	
	private static final Logger logger=LoggerFactory.getLogger(NettyMutilTcpClient.class);
	
	private final Map<Integer, NettyTcpClient> tcpClients=new ConcurrentHashMap<>();
	
	public NettyMutilTcpClient() {
		super();
	}
	
	/**
	 * 添加客户端
	 * @param service
	 * @param config
	 * @param channelInitializer
	 */
	public void addTcpClient(NettyClientService service,NettyClientConfig config,ChannelInitializer<SocketChannel> channelInitializer) {
		NettyTcpClient client=null;
		if(tcpClients.containsKey(config.getId())) {
			client=tcpClients.get(config.getId());
			client.setNettyClientConfig(config);
			return;
		}
		client=new NettyTcpClient(service,channelInitializer,config);
		tcpClients.put(config.getId(), client);
	}
	
	/**
	 * 添加客户端
	 * @param service
	 * @param config
	 */
	public void addTcpClient(NettyClientService service,NettyClientConfig config) {
		this.addTcpClient(service, config, new DefaultClientChannelInitializer(service));
	}
	
	public NettyTcpClient getTcpClient(Integer id) {
		if(!tcpClients.containsKey(id)) {
			return null;
		}
		return tcpClients.get(id);
	}
	
	public void removeTcpClient(Integer id) {
		tcpClients.remove(id);
	}
	
	public boolean containsKey(Integer id) {
		return tcpClients.containsKey(id);
	}
	
	/**
	 * 向服务器发送数据
	 * @param sid  客户端id
	 * @param obj 消息
	 * @return
	 */
	public boolean sendMsg(Integer sid,Object obj) {
		if(!tcpClients.containsKey(sid)) {
			return false;
		}
		NettyTcpClient client=tcpClients.get(sid);
		if(client==null) {
			return false;
		}
		return client.getService().sendMsg(obj);
	}
	
	/**
	 * 检测服务器状态
	 */
	public void checkStatus() {
		tcpClients.forEach((id,client) -> client.checkStatus());
	}
	
	public Map<Integer, NettyTcpClient> getTcpClients(){
		return tcpClients;
	}
}
