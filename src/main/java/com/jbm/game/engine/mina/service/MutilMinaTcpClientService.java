package com.jbm.game.engine.mina.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.session.IoSession;

import com.jbm.game.engine.mina.MinaMultiTcpClient;
import com.jbm.game.engine.mina.config.MinaClientConfig;
import com.jbm.game.engine.mina.config.MinaServerConfig;
import com.jbm.game.engine.mina.handler.DefaultClientProtocolHandler;
import com.jbm.game.engine.mina.message.IDMessage;
import com.jbm.game.engine.server.IMutilTcpClientService;
import com.jbm.game.engine.server.ServerInfo;
import com.jbm.game.engine.server.ServerType;
import com.jbm.game.engine.thread.ThreadPoolExecutorConfig;

/**
 * 多tcp 连接客户端
  * <p>
 * 一般用于子游戏服务器和网关服，所有玩家共享连接
 * </p>
 * @author JiangBangMing
 *
 * 2018年7月12日 上午11:50:56
 */
public class MutilMinaTcpClientService extends MinaClientService implements IMutilTcpClientService<MinaServerConfig>{

	protected MinaMultiTcpClient multiTcpClient=new MinaMultiTcpClient();
	/**网关服务器*/
	protected Map<Integer, ServerInfo> serverMap=new ConcurrentHashMap<>();
	
	public MutilMinaTcpClientService(MinaClientConfig minaClientConfig) {
		super(minaClientConfig);
	}
	
	public MutilMinaTcpClientService(ThreadPoolExecutorConfig threadPoolExecutorConfig,MinaClientConfig minaClientConfig) {
		super(threadPoolExecutorConfig, minaClientConfig);
	}
	
	@Override
	protected void running() {
		
	}
	/**
	 * 移除客户端
	 */
	public void removeTcpClient(int serverId) {
		multiTcpClient.removeTcpClient(serverId);
		serverMap.remove(serverId);
	}
	
	/**
	 * 添加连接服务器
	 */
	public void addTcpClient(ServerInfo serverInfo,int port) {
		this.addTcpClient(serverInfo, port, new MutilTcpProtocolHandler(serverInfo, this));
	}
	
	public void addTcpClient(ServerInfo serverInfo,int port,MutilTcpProtocolHandler ioHandler) {
		if(multiTcpClient.containsKey(serverInfo.getId())) {
			return;
		}
		MinaClientConfig hallMinaClientConfig=this.createMinaClientConfig(serverInfo, port);
		multiTcpClient.addTcpClient(this, hallMinaClientConfig, ioHandler);
	}
	
	@Override
	public Map<Integer, ServerInfo> getServers() {
		return serverMap;
	}
	
	/**
	 * 检测连接状态
	 */
	@Override
	public void checkStatus() {
		multiTcpClient.getTcpClients().values().forEach(cl-> cl.checkStatus());
	}
	
	/**
	 * 广播所有服务器消息：注意，这里并不是向每个session广播，因为有可能有多个连接到同一个服务器
	 * @param obj
	 * @return
	 */
	@Override
	public boolean broadcastMsg(Object obj) {
		if(multiTcpClient==null) {
			return false;
		}
		IDMessage idm=new IDMessage(null, obj, 0);
		serverMap.values().forEach(server ->{
			server.sendMsg(obj);
		});
		return true;
	}
	
	/**
	 *  广播所有服务器消息：注意，这里并不是向每个session广播，因为有可能有多个连接到同一个服务器
	 * @param obj
	 * @param id
	 * @return
	 */
	public boolean broadcastMsg(Object obj,long rid) {
		if(multiTcpClient==null) {
			return false;
		}
		IDMessage idm=new IDMessage(null, obj, rid);
		serverMap.values().forEach(server ->{
			server.sendMsg(idm);
		});
		return true;
	}
	
	/**
	 * 发送消息
	 */
	public boolean sendMsg(Integer serverId,Object msg) {
		if(multiTcpClient==null) {
			return false;
		}
		IDMessage idm=new IDMessage(null, msg, 0);
		return multiTcpClient.sendMsg(serverId, idm);
	}
	/**
	 * 创建连接网关配置文件
	 * @param serverInfo
	 * @param port
	 * @return
	 */
	private MinaClientConfig createMinaClientConfig(ServerInfo serverInfo,int port) {
		MinaClientConfig conf=new MinaClientConfig();
		conf.setType(ServerType.GAME);
		conf.setId(serverInfo.getId());
		conf.setMaxConnectCount(this.getMinaClientConfig().getMaxConnectCount());
		conf.setOrderedThreadPoolExecutorSize(this.getMinaClientConfig().getOrderedThreadPoolExecutorSize());
		MinaClientConfig.MinaClienConnToConfig con=new MinaClientConfig.MinaClienConnToConfig();
		con.setHost(serverInfo.getIp());
		con.setPort(port);
		conf.setConnTo(con);
		return conf;
	}
	/**
	 * 多个连接消息处理器
	 * @author JiangBangMing
	 *
	 * 2018年7月12日 下午1:33:21
	 */
	public class MutilTcpProtocolHandler extends DefaultClientProtocolHandler{
		
		private ServerInfo serverInfo;
		
		public MutilTcpProtocolHandler(ServerInfo serverInfo,MinaClientService service) {
			super(12,service);
			this.serverInfo=serverInfo;
		}
		
		@Override
		public void sessionOpened(IoSession session) {
			super.sessionOpened(session);
			serverInfo.onIoSessionConnect(session);
		}
	}
}
