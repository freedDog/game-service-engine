package com.jbm.game.engine.server;

import java.util.Map;

import com.jbm.game.engine.mina.message.IDMessage;

/**
 * 连接多服务器客户端接口
 * @author JiangBangMing
 *
 * 2018年7月12日 上午11:52:06
 */
public interface IMutilTcpClientService<T extends BaseServerConfig> extends Runnable {

	/**
	 * 移除一个客户端
	 * @param serverId
	 */
	public void removeTcpClient(int serverId);
	
	/**
	 * 添加连接服务器
	 * @param serverInfo
	 * @param port
	 */
	public void addTcpClient(ServerInfo serverInfo,int port);
	
	/**
	 * 服务器列表
	 * @return
	 */
	public Map<Integer, ServerInfo> getServers();
	
	/**
	 * 监测连接状态
	 */
	public void checkStatus();
	
	/**
	 * 广播所有服务器消息，注意，这里并不是每session广播，因为有可能有多个连接到同一个服务器
	 * @param obj
	 * @return
	 */
	public default boolean broadcastMsg(Object obj) {
		IDMessage idm=new IDMessage(null, obj, 0);
		getServers().values().forEach(server ->{
			server.sendMsg(idm);
		});
		return true;
	}
	
	/**
	 * 发送消息
	 * @param serverId 目标服务器id
	 * @param msg
	 * @return
	 */
	public boolean sendMsg(Integer serverId,Object msg);
}
