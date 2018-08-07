package com.jbm.game.engine.mina.websocket;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * 
 * @author JiangBangMing
 *
 * 2018年7月12日 下午1:49:31
 */
public class WebSocketCodecPacket {

	private IoBuffer packet;
	
	public static WebSocketCodecPacket buildPacket(IoBuffer buffer) {
		return new WebSocketCodecPacket(buffer);
	}
	
	private WebSocketCodecPacket(IoBuffer buffer) {
		packet=buffer;
	}

	public IoBuffer getPacket() {
		return packet;
	}
	
	
}
