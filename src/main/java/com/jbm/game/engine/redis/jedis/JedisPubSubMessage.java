package com.jbm.game.engine.redis.jedis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.alibaba.fastjson.JSON;

/**
 * redis 的订阅发布信息
 * @author JiangBangMing
 *
 * 2018年7月13日 下午12:23:44
 */
public class JedisPubSubMessage {

	//消息的目标
	private long id;
	//消息的目标集
	private Set<Long> ids;
	//消息来源的服务器
	private int server;
	//消息目标的服务器
	private int target;
	//消息的值，json 格式
	private String json;
	//属性 key
	private String key;
	//属性 int key
	private int intValue;
	//属性long key
	private long longValue;
	
	public JedisPubSubMessage() {

	}
	
	public JedisPubSubMessage(long id) {
		this.id=id;
	}
	
	public JedisPubSubMessage(long id,int server) {
		this.id=id;
		this.server=server;
	}
	
	public JedisPubSubMessage(long id,int server,int target) {
		this.id=id;
		this.server=server;
		this.target=target;
	}
	
	public JedisPubSubMessage(long id,int server,int target,String key,int intValue) {
		this.id=id;
		this.server=server;
		this.target=target;
		this.key=key;
		this.intValue=intValue;
	}
	
	public JedisPubSubMessage(long id,int server,int target,String key,long longValue) {
		this.id=id;
		this.server=server;
		this.target=target;
		this.key=key;
		this.longValue=longValue;
	}
	
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
	public void addIds(Long... ids) {
		if(this.ids==null) {
			this.ids=new HashSet<>();
		}
		this.ids.addAll(Arrays.asList(ids));
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Set<Long> getIds() {
		return ids;
	}

	public void setIds(Set<Long> ids) {
		this.ids = ids;
	}

	public int getServer() {
		return server;
	}

	public void setServer(int server) {
		this.server = server;
	}

	public int getTarget() {
		return target;
	}

	public void setTarget(int target) {
		this.target = target;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}

	public long getLongValue() {
		return longValue;
	}

	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}
	
}
