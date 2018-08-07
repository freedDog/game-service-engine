package com.jbm.game.engine.mina;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.service.IoHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jbm.game.engine.mina.code.ProtocolCodecFactoryImpl;
import com.jbm.game.engine.mina.config.MinaClientConfig;
import com.jbm.game.engine.mina.service.MinaClientService;

/**
 * 多客户端管理，连接多个服务器
 * @author JiangBangMing
 *
 * 2018年7月10日 上午11:55:07
 */
public class MinaMultiTcpClient {

	private static final Logger logger=LoggerFactory.getLogger(MinaMultiTcpClient.class);
	
	//客户端列表key:服务器id
	private final Map<Integer, MinaTcpClient> tcpClients=new ConcurrentHashMap<>();
	
	public MinaMultiTcpClient() {
		
	}
	
	/**
	 * 添加客户端
	 * @param service
	 * @param config
	 * @param clientProtocolHandler
	 */
	public void addTcpClient(MinaClientService service,MinaClientConfig config,IoHandler clientProtocolHandler) {
		MinaTcpClient client=null;
		if(tcpClients.containsKey(config.getId())) {
			client=tcpClients.get(config.getId());
			client.setMinaClientConfig(config);
			return;
		}
		client=new MinaTcpClient(service, config, clientProtocolHandler);
		tcpClients.put(config.getId(), client);
	}
	
	public void addTcpClient(MinaClientService service,MinaClientConfig config,IoHandler clientProtocolHandler,
			ProtocolCodecFactoryImpl factory) {
		MinaTcpClient client=null;
		if(tcpClients.containsKey(config.getId())) {
			client=tcpClients.get(config.getId());
			client.setMinaClientConfig(config);
			return;
		}
		client=new MinaTcpClient(service, config, clientProtocolHandler,factory);
		tcpClients.put(config.getId(), client);
	}
	
	
	public void removeTcpClient(Integer id) {
		tcpClients.remove(id);
	}
	
	/**
	 * 是否存在对应的客户端
	 * @param id  key
	 * @return
	 */
	public boolean containsKey(Integer id) {
		return tcpClients.containsKey(id);
	}
	
	/**
	 * 向服务器发送数据
	 * @param sid
	 * @param obj
	 * @return
	 */
	public boolean sendMsg(Integer sid,Object obj) {
		if(!tcpClients.containsKey(sid)) {
			return false;
		}
		MinaTcpClient client=tcpClients.get(sid);
		if(client==null) {
			return false;
		}
		
		return client.getService().sendMsg(obj);
	}
	
	/**
	 * 状态监测
	 */
	public void checkStatus() {
		tcpClients.values().forEach(c->c.checkStatus());
	}
	
	public Map<Integer, MinaTcpClient> getTcpClients(){
		return tcpClients;
	}
	
}
