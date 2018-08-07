package com.jbm.game.engine.handler;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.http.HttpRequestImpl;
import org.apache.mina.http.api.HttpRequest;
import org.apache.mina.http.api.HttpStatus;

import com.alibaba.fastjson.JSON;
import com.jbm.game.engine.mina.code.HttpResponseImpl;

/**
 * httip 请求
 * @author JiangBangMing
 *
 * 2018年7月3日 下午8:06:14
 */
public abstract class HttpHandler implements IHandler{

	private HttpResponseImpl response;//返回消息
	
	private IoSession session;//消息来源
	
	private HttpRequest request;//请求消息
	
	private long createTime;
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	@Override
	public IoSession getSession() {
		return this.session;
	}
	
	@Override
	public void setSession(IoSession session) {
		this.session=session;
	}
	
	@Override
	public HttpRequestImpl getMessage() {
		return (HttpRequestImpl)this.request;
	}
	
	@Override
	public void setMessage(Object message) {
		if(message instanceof HttpRequestImpl) {
			this.request=(HttpRequestImpl)message;
		}
	}
	
	@Override
	public HttpResponseImpl getParameter() {
		if(this.response==null) {
			this.response=new HttpResponseImpl();
		}
		return this.response;
	}
	
	@Override
	public void setParameter(Object parameter) {
		if(parameter instanceof HttpResponseImpl) {
			this.response=(HttpResponseImpl)parameter;
		}
	}
	
	@Override
	public long getCreateTime() {
		return this.createTime;
	}
	
	@Override
	public void setCreateTime(long time) {
		this.createTime=time;
	}
	/**
	 * 没有返回值的情况下处理
	 * @return
	 */
	protected HttpResponseImpl errResponseMessage() {
		HttpResponseImpl response=new HttpResponseImpl();
		response.setStatus(HttpStatus.CLIENT_ERROR_NOT_FOUND);
		return response;
	}
	/**
	 * 发送消息
	 */
	public void response() {
		if(response!=null) {
			session.write(response);
		}else {
			session.write(errResponseMessage());
		}
	}
	
	/**
	 * 返回状态消息
	 */
	public void responseWithStatus() {
		if(this.response!=null) {
			if(this.response.bodyLength()<1) {
				this.response.appendBody(this.response.getStatus().line());
			}
			session.write(response);
		}else {
			session.write(errResponseMessage());
		}
	}
	
	/**
	 * 获取字符串参数
	 * @param field
	 * @return
	 */
	public String getString(String field) {
		return getMessage().getParameter(field);
	}
	
	/**
	 * 
	 * @param field
	 * @return默认值0
	 */
	public int getInt(String field) {
		String string=getString(field);
		if(string==null) {
			return 0;
		}
		return Integer.parseInt(string);
	}
	
	public long getLong(String field) {
		String string=getString(field);
		if(string==null) {
			return 0;
		}
		return Long.parseLong(string);
	}
	
	public double getDouble(String field) {
		String string=getString(field);
		if(string==null) {
			return 0;
		}
		return Double.parseDouble(string);
	}
	
	/**
	 * 发送消息
	 * @param object
	 */
	public void sendMsg(Object object) {
		getParameter().appendBody(JSON.toJSONString(object));
		response();
	}
}
