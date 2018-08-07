package com.jbm.game.engine.server;

import java.util.Objects;

import org.simpleframework.xml.Element;

/**
 * mina 基本配置
 * @author JiangBangMing
 *
 * 2018年7月4日 下午2:30:19
 */
public abstract class BaseServerConfig {

	//这3个是必须配置的
	//服务器标识
	@Element(required=false)
	protected int id;
	
	//服务器名称
	@Element(required=false)
	protected String name="无";
	
	//服务器渠道
	@Element(required=false)
	protected String channel;
	
	//服务器版本
	@Element(required=false)
	protected String version="0.0.1";
	
	//接受数据缓冲大小
	@Element(required=false)
	protected int receiveBufferSize=1048576;
	
	//发送数据缓冲大小
	@Element(required=false)
	protected int sendBufferSize=1048576;
	
	//接收数据最大字节数
	@Element(required=false)
	protected int maxReadSize=1048576;
	
	//发送数据缓冲消息数
	@Element(required=false)
	protected int maxScheduledWriteMessage=1024;
	
	//eq id + channel
	@Override
	public int hashCode() {
		int hash=7;
		hash=41*hash+this.id;
		hash=41*hash+Objects.hashCode(this.channel);
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==null) {
			return false;
		}
		if(getClass()!=obj.getClass()) {
			return false;
		}
		final BaseServerConfig other=(BaseServerConfig)obj;
		if(this.id!=other.id) {
			return false;
		}
		
		return true;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getReceiveBufferSize() {
		return receiveBufferSize;
	}

	public void setReceiveBufferSize(int receiveBufferSize) {
		this.receiveBufferSize = receiveBufferSize;
	}

	
	public int getSendBufferSize() {
		return sendBufferSize;
	}

	public void setSendBufferSize(int sendBufferSize) {
		this.sendBufferSize = sendBufferSize;
	}

	public int getMaxReadSize() {
		return maxReadSize;
	}

	public void setMaxReadSize(int maxReadSize) {
		this.maxReadSize = maxReadSize;
	}

	public int getMaxScheduledWriteMessage() {
		return maxScheduledWriteMessage;
	}

	public void setMaxScheduledWriteMessage(int maxScheduledWriteMessage) {
		this.maxScheduledWriteMessage = maxScheduledWriteMessage;
	}
	
}
