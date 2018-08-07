package com.jbm.game.engine.mina.code;

import java.util.function.Predicate;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.jbm.game.engine.mina.message.IDMessage;
import com.jbm.game.engine.util.MsgUtil;

/**
 * 消息编码
 * @author JiangBangMing
 *
 * 2018年7月5日 下午12:32:30
 */
public class ProtocolEncoderImpl implements ProtocolEncoder{
	
	private static Logger logger=LoggerFactory.getLogger(ProtocolDecoderImpl.class);
	
	//允许的最大堆积未发消息条数
	private int maxScheduledWriteMessages=256;
	//当超过设置的最大堆积消息条数时的处理
	protected Predicate<IoSession> overScheduledWriteBytesHandler;

	public ProtocolEncoderImpl() {

	}
	
	@Override
	public void dispose(IoSession arg0) throws Exception {
		
	}

	@Override
	public void encode(IoSession session, Object obj, ProtocolEncoderOutput out) throws Exception {
		if(getOverScheduledWriteBytesHandler()!=null&&session.getScheduledWriteMessages()>getMaxScheduledWriteMessages()&&getOverScheduledWriteBytesHandler().test(session)) {
			return;
		}
		
		IoBuffer buf=null;
		if(obj instanceof Message) {
			buf=MsgUtil.toIobuffer((Message)obj);
		}else if(obj instanceof IDMessage) {
			buf=MsgUtil.toIobuffer((IDMessage)obj);
		}else if(obj instanceof IoBuffer) {//必须符合完整的编码格式
			buf=(IoBuffer)obj;
		}else if(obj instanceof byte[]) {//必须符合除去消息长度后的编码格式
			byte[] data=(byte[])obj;
			buf=IoBuffer.allocate(data.length+4);
			buf.putInt(data.length);
			buf.put(data);
		}else {
			logger.warn("未知的数据类型");
			return;
		}
		
		if(buf!=null&&session.isConnected()) {
			buf.rewind();
			out.write(buf);;
			out.flush();
		}
	}

	public int getMaxScheduledWriteMessages() {
		return maxScheduledWriteMessages;
	}

	public void setMaxScheduledWriteMessages(int maxScheduledWriteMessages) {
		this.maxScheduledWriteMessages = maxScheduledWriteMessages;
	}

	public Predicate<IoSession> getOverScheduledWriteBytesHandler() {
		return overScheduledWriteBytesHandler;
	}

	public void setOverScheduledWriteBytesHandler(Predicate<IoSession> overScheduledWriteBytesHandler) {
		this.overScheduledWriteBytesHandler = overScheduledWriteBytesHandler;
	}
	
	

}
