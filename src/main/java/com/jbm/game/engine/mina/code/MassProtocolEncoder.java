package com.jbm.game.engine.mina.code;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jbm.game.engine.mina.message.MassMessage;
import com.jbm.game.engine.util.MsgUtil;

/**
 * 群发消息
 * @author JiangBangMing
 *
 * 2018年7月5日 下午12:53:47
 */
public class MassProtocolEncoder extends ProtocolEncoderImpl{

	private static Logger logger=LoggerFactory.getLogger(MassProtocolEncoder.class);
	
	public MassProtocolEncoder() {

	}
	
	@Override
	public void encode(IoSession session, Object obj, ProtocolEncoderOutput out) throws Exception {
		if(getOverScheduledWriteBytesHandler()!=null&&session.getScheduledWriteMessages()>getMaxScheduledWriteMessages()&&getOverScheduledWriteBytesHandler().test(session)) {
			return;
		}
		IoBuffer buf=null;
		if(obj instanceof MassMessage) {
			buf=MsgUtil.toIobuffer((MassMessage)obj);
		}else {
			logger.warn("未知的消息类型");
			return;
		}
		if(buf!=null&&session.isConnected()) {
			buf.rewind();
			out.write(buf);
			out.flush();
		}
	}
}
