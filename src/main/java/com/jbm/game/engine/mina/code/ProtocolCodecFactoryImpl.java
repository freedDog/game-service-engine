package com.jbm.game.engine.mina.code;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * 消息解析工厂
 * @author JiangBangMing
 *
 * 2018年7月5日 下午12:43:04
 */
public class ProtocolCodecFactoryImpl implements ProtocolCodecFactory{
	
	protected final ProtocolDecoderImpl decoder;
	protected final ProtocolEncoderImpl encoder;

	public ProtocolCodecFactoryImpl(ProtocolDecoderImpl decoder,ProtocolEncoderImpl encoder) {
		this.decoder=decoder;
		this.encoder=encoder;
	}
	
	@Override
	public ProtocolDecoder getDecoder(IoSession arg0) throws Exception {
		return getDecoder();
	}

	@Override
	public ProtocolEncoder getEncoder(IoSession arg0) throws Exception {
		return getEncoder();
	}

	public ProtocolDecoderImpl getDecoder() {
		return decoder;
	}

	public ProtocolEncoderImpl getEncoder() {
		return encoder;
	}

	
}
