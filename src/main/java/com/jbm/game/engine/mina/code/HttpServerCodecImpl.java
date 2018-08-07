package com.jbm.game.engine.mina.code;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * http 消息解编码工厂
 * @author JiangBangMing
 *
 * 2018年7月5日 下午4:49:31
 */
public class HttpServerCodecImpl extends ProtocolCodecFilter{

	private static final String DECODER_STATE_ATT="http.ds";
	
	private static final String PARTIAL_HEAD_ATT="http.ph";
	
	private static ProtocolEncoder encoder=new HttpServerEncoderImpl();
	private static ProtocolDecoder decoder=new HttpServerDecoderImpl();
	
	
	
	public HttpServerCodecImpl() {
		super(encoder, decoder);
	}
	
	@Override
	public void sessionClosed(NextFilter nextFilter, IoSession session) throws Exception {
		super.sessionClosed(nextFilter, session);
		session.removeAttribute(DECODER_STATE_ATT);
		session.removeAttribute(PARTIAL_HEAD_ATT);
	}

}
