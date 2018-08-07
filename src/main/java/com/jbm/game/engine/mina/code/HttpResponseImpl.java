package com.jbm.game.engine.mina.code;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.mina.http.api.HttpResponse;
import org.apache.mina.http.api.HttpStatus;
import org.apache.mina.http.api.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http 返回消息
 * @author JiangBangMing
 *
 * 2018年7月3日 下午8:10:45
 */
public class HttpResponseImpl implements HttpResponse{
	
	private static final Logger logger=LoggerFactory.getLogger(HttpResponseImpl.class);
	
	private final HashMap<String, String> headers=new HashMap<>();//头消息
	
	private final HttpVersion version=HttpVersion.HTTP_1_1;
	
	private HttpStatus status=HttpStatus.CLIENT_ERROR_FORBIDDEN;
	
	private final StringBuffer bodyStringBuffer;//内容
	
	private byte[] body;//内容
	
	public HttpResponseImpl() {
		headers.put("Server", "HttpServer ("+"Mina 2.013"+")");
		headers.put("Cache-Control", "private");
		headers.put("Content-Type", "text/html;charset=UTF-8");
		headers.put("Keep-Alive", "500");
		headers.put("Date", new SimpleDateFormat("EEE,dd,MMM,yyyy HH:mm:ss zzz").format(new Date()));
		headers.put("Last-Modified",new SimpleDateFormat("EEE,dd,MMM,yyyy HH:mm:ss zzz").format(new Date()));
		status=HttpStatus.SUCCESS_OK;
		bodyStringBuffer=new StringBuffer();
	}
	
	public void setContentType(String contentType) {
		headers.put("Content-Type", contentType);
	}
	
	/**
	 * 追加内容
	 * @param s
	 * @return
	 */
	public HttpResponseImpl appendBody(String s) {
		this.bodyStringBuffer.append(s);
		return this;
	}
	
	/**
	 * 内容长度
	 * @return
	 */
	public int bodyLength() {
		return bodyStringBuffer.length();
	}
	
	@Override
	public boolean containsHeader(String name) {
		return this.headers.containsKey(name);
	}

	public byte[] getBody() {
		try {
			if(body==null) {
				body=this.bodyStringBuffer.toString().getBytes("utf-8");
			}
		}catch (Exception e) {
			logger.error("getBody ",e);
		}
		return body;
	}
	
	@Override
	public String getContentType() {
		return headers.get("Content-type");
	}

	@Override
	public String getHeader(String name) {
		return headers.get(name);
	}

	@Override
	public Map<String, String> getHeaders() {
		return this.headers;
	}

	@Override
	public HttpVersion getProtocolVersion() {
		return this.version;
	}

	@Override
	public boolean isKeepAlive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public HttpStatus getStatus() {
		return this.status;
	}
	
	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append("HTTP RESPONSE STATUS: ").append(status).append("\n");
		sb.append("VERSION: ").append(version).append("\n");
		sb.append("-- HEADER -- \n");
		
		for(Map.Entry<String, String> entry:headers.entrySet()) {
			sb.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
		}
		return sb.toString();
	}

	public void setStatus(HttpStatus status) {
		this.status = status;
	}
	
}
