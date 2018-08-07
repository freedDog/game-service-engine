package com.jbm.game.engine.mina.message;

import java.util.Collection;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * 群发消息
 * @author JiangBangMing
 *
 * 2018年7月3日 下午7:43:54
 */
public class MassMessage {

	private final IoBuffer buffer;
	private final Collection<Long> targets;//用户ID
	
	public MassMessage(IoBuffer buffer,Collection<Long> targets) {
		this.buffer=buffer;
		this.targets=targets;
		this.buffer.rewind();
	}
	
	public int getLength() {
		return buffer.remaining()+targets.size()*8;
	}
	
	public int getBuffLength() {
		return buffer.remaining();
	}
	
	public IoBuffer getBuffer() {
		return buffer;
	}
	
	public Collection<Long> getTargets(){
		return targets;
	}
}
