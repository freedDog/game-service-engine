package com.jbm.game.engine.mina.code;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 群发消息
 * @author JiangBangMing
 *
 * 2018年7月5日 下午12:47:02
 */
public class MassProtocolDecoder extends ProtocolDecoderImpl{

	private static final Logger logger=LoggerFactory.getLogger(MassProtocolDecoder.class);
	
	public MassProtocolDecoder() {
		maxReadSize=1024*1024*5;
	}
	
	@Override
	protected boolean doDecode(IoSession session, IoBuffer ib, ProtocolDecoderOutput out) throws Exception {
		if(ib.remaining()<4) {
			return false;
		}
		ib.mark();
		int length=ib.getInt();
		if(length<1||length>maxReadSize) {
			int id=ib.getInt();
			ib.clear();
			logger.warn("消息解析异常:长度{} ,id{},大于长度maxReadSize {}",length,id,maxReadSize);
			session.closeNow();
			return false;
		}
		if(ib.remaining()<length) {
			ib.reset();
			return false;
		}
		decodeBytes(length, ib, out);
		return true;
	}
	
}
