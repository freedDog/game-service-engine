package com.jbm.game.engine.mina.code;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.http.HttpServerEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息编码
 * @author JiangBangMing
 *
 * 2018年7月5日 下午4:36:09
 */
public class HttpServerEncoderImpl extends HttpServerEncoder{

	private static final Logger logger=LoggerFactory.getLogger(HttpServerEncoderImpl.class);
	private static final CharsetEncoder ENCODER=Charset.forName("UTF-8").newEncoder();
	private static final byte[] CRLE= {13,10};
	private static final String CONTENTLENGTH="Content-Length: ";
	
	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		if(logger.isDebugEnabled()) {
			logger.debug("encode {}",message.getClass().getCanonicalName());
		}
		if(message instanceof HttpResponseImpl) {
			HttpResponseImpl msg=(HttpResponseImpl)message;
			IoBuffer buf=IoBuffer.allocate(128).setAutoExpand(true);
			buf.putString(msg.getStatus().line(), ENCODER);
			for(Map.Entry<String, String> header:msg.getHeaders().entrySet()) {
				buf.putString((CharSequence)header.getKey(), ENCODER);
				buf.putString(": ", ENCODER);
				buf.putString((CharSequence)header.getValue(), ENCODER);
				buf.put(CRLE);
			}
			if(msg.getBody()!=null) {
				buf.putString(CONTENTLENGTH, ENCODER);
				buf.putString(String.valueOf(msg.getBody().length), ENCODER);
				buf.put(CRLE);
			}
			buf.put(CRLE);
			if(msg.getBody()!=null) {
				buf.put(msg.getBody());
			}
			buf.flip();
			out.write(buf);
		}
	}
	@Override
	public void dispose(IoSession session) throws Exception {
		super.dispose(session);
	}
}
