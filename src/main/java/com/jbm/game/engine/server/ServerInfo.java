package com.jbm.game.engine.server;

import java.text.SimpleDateFormat;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.annotation.JSONField;
import com.jbm.game.engine.util.MsgUtil;

import io.netty.channel.Channel;

/**
 * 服务器信息
 * 封装了mina和netty连接会话
 * @author JiangBangMing
 *
 * 2018年7月12日 上午11:54:14
 */
public class ServerInfo {

	private final static Logger logger=LoggerFactory.getLogger(ServerInfo.class);
	
	private static final SimpleDateFormat DF=new SimpleDateFormat("yyyy-MM-dd-HH-mm");
	//服务器ID
	private int id;
	//服务器名称
	protected String name;
	//地址
	private String ip;
	//外网地址
	private String wwwip;
	//端口
	private int port;
	//当前状态  1 表示维护, 0 表示正常
	private int state=0;
	//http 端口
	private int httpPort;
	//最大用户人数
	private int maxUserCount;
	//在线人数
	@JSONField(serialize=true)
	private int online;
	//服务器类型
	private int type;
	//服务器类型
	private int freeMemory;
	//可用内存
	private int totalMemory;
	//版本号，用于判断客户端连接那个服务器
	private String version;
	
	@JSONField(serialize=false)
	private transient IoSession session;
	//客户端多个连接管理
	@JSONField(serialize=false)
	protected transient Queue<IoSession> sessions;
	@JSONField(serialize=false)
	private transient Channel channel;
	
	//客户端多个连接管理
	@JSONField(serialize=false)
	protected transient Queue<Channel> channels;
	
	public ServerInfo() {
		
	}
	
	@JSONField(serialize=false)
	public void onIoSessionConnect(IoSession session) {
		if(sessions==null) {
			sessions=new ConcurrentLinkedQueue<>();
		}
		if(!sessions.contains(session)) {
			sessions.add(session);
		}
	}
	
	@JSONField(serialize=false)
	public void onChannelActive(Channel channel) {
		if(channels==null) {
			channels=new ConcurrentLinkedQueue<>();
		}
		if(!channels.contains(channel)) {
			channels.add(channel);
		}
	}
	/**
	 * 获取连接列表中最空闲的有效的连接
	 * @return
	 */
	public IoSession getMostIdleIoSession() {
		if(sessions==null) {
			return null;
		}
		IoSession session=null;
		sessions.stream().sorted(MsgUtil.sessionIdleComparator);
		while(session==null&&!sessions.isEmpty()) {
			session=sessions.poll();
			if(logger.isDebugEnabled()) {
				logger.debug("空闲session{}",session.getId());
			}
			if(session!=null&&session.isConnected()) {
				sessions.offer(session);
				break;
			}
		}
		return session;
	}
	
	/**
	 * 获取空闲连接
	 * @return
	 */
	public Channel getMostIdIdleChannel() {
		if(channels==null) {
			return null;
		}
		Channel channel=null;
		channels.stream().sorted((c1,c2)->(int)(c1.bytesBeforeUnwritable()-c2.bytesBeforeUnwritable()));
		while(channel==null&&!channels.isEmpty()) {
			channel=channels.poll();
			if(channel!=null&&channel.isActive()) {
				channels.offer(channel);
				break;
			}
		}
		return channel;
	}
	
	public void sendMsg(Object message) {
		IoSession session=getSession();
		if(session!=null) {
			session.write(message);
		}else if(getChannel()!=null) {
			getChannel().writeAndFlush(message);
		}else {
			logger.warn("服务器:"+name+" 连接会话为空");
		}
	}
	@JSONField(serialize=false)
	public Channel getChannel() {
		if(channel==null||!channel.isActive()) {
			this.channel=getMostIdIdleChannel();
		}
		return this.channel;
	}
	@JSONField(serialize=false)
	public void setChannel(Channel channel) {
		this.channel=channel;
	}
	@JSONField(serialize=false)
	public IoSession getSession() {
		if(session==null||!session.isActive()) {
			this.session=getMostIdleIoSession();
		}
		return session;
	}
	@JSONField(serialize=false)
	public void setSession(IoSession session) {
		this.session=session;
	}
	
	
	public String getHttpUrl(String content) {
		StringBuilder sb=new StringBuilder("http://").append(getIp()).append(":").append(getHttpPort()).append("/")
				.append(content);
		return sb.toString().trim();
	}
	
	@Override
	public String toString() {
		return "ServerInfo [id=" + id + ", name=" + name + ", ip=" + ip + ", wwwip=" + wwwip + ", port=" + port
				+ ", state=" + state + ", httpPort=" + httpPort + ", maxUserCount=" + maxUserCount + ", type=" + type
				+ "]";
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

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getWwwip() {
		return wwwip;
	}

	public void setWwwip(String wwwip) {
		this.wwwip = wwwip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}

	public int getMaxUserCount() {
		return maxUserCount;
	}

	public void setMaxUserCount(int maxUserCount) {
		this.maxUserCount = maxUserCount;
	}

	public int getOnline() {
		return online;
	}

	public void setOnline(int online) {
		this.online = online;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getFreeMemory() {
		return freeMemory;
	}

	public void setFreeMemory(int freeMemory) {
		this.freeMemory = freeMemory;
	}

	public int getTotalMemory() {
		return totalMemory;
	}

	public void setTotalMemory(int totalMemory) {
		this.totalMemory = totalMemory;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Queue<IoSession> getSessions() {
		return sessions;
	}

	public void setSessions(Queue<IoSession> sessions) {
		this.sessions = sessions;
	}

	public Queue<Channel> getChannels() {
		return channels;
	}

	public void setChannels(Queue<Channel> channels) {
		this.channels = channels;
	}

	public static SimpleDateFormat getDf() {
		return DF;
	}
	
}
