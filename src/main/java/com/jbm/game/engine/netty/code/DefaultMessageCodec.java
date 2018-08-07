package com.jbm.game.engine.netty.code;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.jbm.game.engine.mina.message.IDMessage;
import com.jbm.game.engine.util.MsgUtil;
import com.jbm.game.engine.util.TimeUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

/**
 * 内部默认消息解码器
  * <br>消息有带角色ID的，有不带角色ID的
 * @note IDMessage mina、netty通用
 * @author JiangBangMing
 *
 * 2018年7月13日 下午4:07:20
 */
public class DefaultMessageCodec extends ByteToMessageCodec<Object>{
	
	private static final Logger logger=LoggerFactory.getLogger(DefaultMessageCodec.class);
	private int headerLength=12;//消息长度
	
	public DefaultMessageCodec() {
		super();
	}
	
	/**
	 * 
	 * @param headerLength 4 消息ID 12 角色ID+消息ID
	 */
	public DefaultMessageCodec(int headerLength) {
		this.headerLength=headerLength;
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if(in.readableBytes()<1) {//空包不处理
			return;
		}
		long start=TimeUtil.currentTimeMillis();
		int length=in.readInt();
		long userID=0;
		if(headerLength==12) {
			userID=in.readLong();
		}
		int msgId=in.readInt();
		int bLen=length-headerLength;
		byte[] data=new byte[bLen];
		in.readBytes(data);
		IDMessage msg=new IDMessage(ctx.channel(), data, userID,msgId);
		out.add(msg);
		if(logger.isDebugEnabled()) {
			logger.debug("解密:{}",(TimeUtil.currentTimeMillis()-start));
		}
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Object obj, ByteBuf out) throws Exception {
		//使用mina进行转换
		long start=TimeUtil.currentTimeMillis();
		byte[] bytes=null;//消息体
		if(obj instanceof IDMessage) {//消息头12 消息ID+角色ID
			bytes=MsgUtil.toIobuffer((IDMessage)obj).array();
		}else if(obj instanceof Message) {//消息头  3  消息id
			bytes=MsgUtil.toIobuffer((Message)obj).array();
		}
		if(bytes!=null) {
			out.writeBytes(bytes);//消息体
		}
	}
}
