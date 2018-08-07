package com.jbm.game.engine.mq;

import org.simpleframework.xml.Element;

/**
 * MQ 配置
 * @author JiangBangMing
 *
 * 2018年7月12日 下午6:53:28
 */
public class MQConfig {

	//用户名
	@Element(required=true)
	private String user="jzy";
	
	//密码
	@Element(required=true)
	private String password="111";
	
	//ip地址
	@Element(required=true)
	private String host="127.0.0.1";
	
	//端口
	@Element(required=true)
	private String port="61616";
	
	//协议类型
	@Element(required=false)
	private String protocol="tcp";
	
	//队列名称
	@Element(required=false)
	private String queueName="hall";

	public String getMqConnectionUrl() {
		return this.protocol+"://"+this.getHost()+":"+this.getPort();
	}
	
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	
	
}
