package com.jbm.game.engine.mq;

import javax.jms.Connection;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jbm.game.engine.util.FileUtil;

/**
 * MQ服务
 * @author JiangBangMing
 *
 * 2018年7月12日 下午6:58:04
 */
public abstract class MQService {

	private static final Logger logger=LoggerFactory.getLogger(MQService.class);
	protected ActiveMQConnectionFactory activeMQConnectionFactory;//连接工厂
	protected Connection connection;//连接
	protected MQConfig mqConfig;//配置
	
	public MQService(MQConfig mqConfig) {
		this.mqConfig=mqConfig;
		this.activeMQConnectionFactory=new ActiveMQConnectionFactory(mqConfig.getMqConnectionUrl());
	}
	
	public MQService(String configPath) {
		mqConfig=FileUtil.getConfigXML(configPath, "mqConfig.xml",MQConfig.class);
		if(mqConfig==null) {
			throw new RuntimeException(String.format("配置文件%s /mqConfig.xml 未配置", configPath));
		}
		
		this.activeMQConnectionFactory=new ActiveMQConnectionFactory(mqConfig.getMqConnectionUrl());
	}
	
	/**
	 * 获取连接
	 * @return
	 */
	public final Connection getConnection() {
		try {
			if(connection==null) {
				connection=activeMQConnectionFactory.createConnection(mqConfig.getUser(), mqConfig.getPassword());
			}
		}catch (Exception e) {
			logger.error("MQ Connection",e);
			connection=null;
		}
		return connection;
	}
	
	/**
	 * 关闭连接
	 */
	public void closeConnection() {
		if(connection!=null) {
			try {
				connection.close();
			}catch (Exception e) {
				logger.error("closeConnection",e);
			}
			connection=null;
		}
	}
}
