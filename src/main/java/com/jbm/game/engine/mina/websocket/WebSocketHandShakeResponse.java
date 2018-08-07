package com.jbm.game.engine.mina.websocket;

/**
 * 围绕一个字符串来表示一个websocket握手响应服务器到浏览器。
 * @author JiangBangMing
 *
 * 2018年7月12日 下午1:46:58
 */
public class WebSocketHandShakeResponse {

	private String response;
	
	public WebSocketHandShakeResponse(String response) {
		this.response=response;
	}

	public String getResponse() {
		return response;
	}
	
	
}
