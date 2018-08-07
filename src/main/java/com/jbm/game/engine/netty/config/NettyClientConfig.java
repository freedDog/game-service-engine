package com.jbm.game.engine.netty.config;

import org.simpleframework.xml.Element;

import com.jbm.game.engine.server.BaseServerConfig;
import com.jbm.game.engine.server.ServerType;

/**
 * netty 客户端配置
 * @author JiangBangMing
 *
 * 2018年7月13日 下午4:22:24
 */
public class NettyClientConfig extends BaseServerConfig{

	//工作线程数
	@Element(required=false)
	private int groupThreadNum=1;
	//当前服务器的类型，如果当前服务器是 gameserver,那么对应ServerType,GameServer=10;
	@Element(required=false)
	private ServerType type=ServerType.GATE;
	//其他配置，如配置服务器允许开启的地图
	@Element(required=false)
	private String info;
	//是否重定向
	@Element(required=false)
	private boolean tcpNoDealy=true;
	//ip
	@Element(required=false)
	private String ip="127.0.0.1";
	//端口
	@Element(required=false)
	private int port=8080;
	//客户端创建的最大连接数
	@Element(required=false)
	private int maxConnectCount=1;
	public int getGroupThreadNum() {
		return groupThreadNum;
	}
	public void setGroupThreadNum(int groupThreadNum) {
		this.groupThreadNum = groupThreadNum;
	}
	public ServerType getType() {
		return type;
	}
	public void setType(ServerType type) {
		this.type = type;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public boolean isTcpNoDealy() {
		return tcpNoDealy;
	}
	public void setTcpNoDealy(boolean tcpNoDealy) {
		this.tcpNoDealy = tcpNoDealy;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public int getMaxConnectCount() {
		return maxConnectCount;
	}
	public void setMaxConnectCount(int maxConnectCount) {
		this.maxConnectCount = maxConnectCount;
	}
	
	
}
