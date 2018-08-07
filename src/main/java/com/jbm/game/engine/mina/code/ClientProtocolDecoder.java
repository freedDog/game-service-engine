package com.jbm.game.engine.mina.code;

import java.nio.ByteOrder;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jbm.game.engine.util.CipherUtil;
import com.jbm.game.engine.util.IntUtil;
import com.jbm.game.engine.util.MsgUtil;

/**
 * 游戏客服端消息解码
  * 包长度（2）+消息ID（4）+消息长度（4）+消息内容 <br>
 * 返回byte数组已去掉包长度
 * </p>
 * TODO 加解密
 * @author JiangBangMing
 *
 * 2018年7月5日 下午5:00:21
 */
public class ClientProtocolDecoder extends ProtocolDecoderImpl{

	private static final Logger logger=LoggerFactory.getLogger(ClientProtocolDecoder.class);
	private static final String START_TIME="start_time";//消息开始时间
	private static final String RECEIVE_COUNT="receive_count";//消息接受次数
	
	public static final byte[] AES_KEY = "vWf7g1Gt701h0.#0".getBytes();
	public static final byte[] AES_IV = "rgnHV16#8HQFc&16".getBytes();
	
	//每秒钟最大接受消息数
	private int maxCountPerSecond=100;
	
	public ClientProtocolDecoder() {

	}
	
	@Override
	protected boolean doDecode(IoSession session, IoBuffer ib, ProtocolDecoderOutput out) throws Exception {
		
		int readAbleLen=ib.remaining();
		if(readAbleLen<2) {
			return false;
		}
		ib.mark();//标记阅读位置
		byte[] bs=new byte[2];
		ib.get(bs,0,2);
		short packageLength=IntUtil.bigEndianByteToShort(bs,0,2);
		if(packageLength<1|| packageLength>maxReadSize) {
			logger.warn("消息包长度:{}",packageLength);
			ib.clear();
			session.closeNow();
			return false;
		}
		
		if(ib.remaining()<packageLength) {//消息长度不够，重置位置
			ib.reset();
			return false;
		}
		//消息id (4个字节)+protobufLength（4个字节）+签名数据长度(4个字节)+签名数据+截取签名长度(4个字节）
		bs=new byte[packageLength];
		ib.get(bs);
		int protobufLength=IntUtil.bigEndianByteToInt(bs, 4, 4);
		if(packageLength>protobufLength+8) {
			if(logger.isDebugEnabled()) {
				logger.debug("消息签名验证");
			}
			if(checkMsgSign(bs, protobufLength)) {
				byte[] datas=new byte[8+protobufLength];
				System.arraycopy(bs, 0, datas, 0, datas.length);
				out.write(bs);
			}else {
				session.closeNow();
			}
		}else {
			out.write(bs);
		}
		
		//消息频率检查
		if(!checkMsgFrequency(session)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 检测签名
	 * @param bytes
	 * 			消息Id(4字节)+protobufLength(4字节)+消息体+时间戳(8字节)+签名数据长度(4字节)+签名数据+截取签名长度(4字节)
	 * @param protobufLength
	 * @return
	 * @throws Exception
	 */
	private boolean checkMsgSign(byte[] bytes,int protobufLength) throws Exception{
		//客户端时间戳
		long timeStamp=IntUtil.bytes2Long(bytes, 8+protobufLength, 8, ByteOrder.LITTLE_ENDIAN);
		//计算签名
		String sign1=calculateSign(bytes, timeStamp);
		//解密签名数组
		int len_md5_data=IntUtil.bigEndianByteToInt(bytes, 16+protobufLength, 4);
		byte[] bytesMD5=new byte[len_md5_data];
		System.arraycopy(bytes, 20+protobufLength, bytesMD5, 0, len_md5_data);
		bytesMD5=decryptAES(bytesMD5);
		
		//截取签名
		int len_clear_sign=IntUtil.bigEndianByteToInt(bytes, 20+protobufLength+len_md5_data, 4);
		byte[] clearSignBytes=new byte[len_clear_sign];
		System.arraycopy(bytesMD5, 0, clearSignBytes, 0, len_clear_sign);
		String sign2=new String(clearSignBytes,"UTF-8");
		
		//检查签名是否一致
		if(!sign1.equals(sign2)) {
			logger.info("----------签名验证失败!"+Arrays.toString(bytes));
			return false;
		}
		return true;
	}
	
	private String calculateSign(byte[] b,long timeStamp) {
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<b.length;i++) {
			int c=b[i];
			if(c<0) {
				c+=256;
			}
			sb.append(c);
		}
		sb.append(timeStamp);
		return CipherUtil.MD5Encode(sb.toString().toUpperCase());
	}
	
	/**
	 * AES 解密
	 * @param bytes
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptAES(byte[] bytes) throws Exception{
		try {
			Cipher cipher=Cipher.getInstance("AES/CBC/NoPadding");
			SecretKeySpec keyspec=new SecretKeySpec(AES_KEY, "AES");
			IvParameterSpec ivspec=new IvParameterSpec(AES_IV);
			cipher.init(Cipher.DECRYPT_MODE, keyspec,ivspec);
			byte[] original=cipher.doFinal(bytes);
			return original;
		}catch (Exception e) {
			logger.error("",e);
		}
		return null;
	}
	/**
	 * 检测玩家消息发送频率
	 * @param session
	 * @return
	 */
	private boolean checkMsgFrequency(IoSession session) {
		int count=0;
		long startTime=0L;
		if(session.containsAttribute(START_TIME)) {
			startTime=(long)session.getAttribute(START_TIME);
		}
		if(session.containsAttribute(RECEIVE_COUNT)) {
			count=(int)session.getAttribute(RECEIVE_COUNT);
		}
		long interval=session.getLastReadTime()-startTime;
		if(interval>1000L) {
			if(count>getMaxCountPerSecond()) {
				MsgUtil.close(session, "%s %d--> %dms内消息过于频繁:%d,超过次数：%d\", MsgUtil.getIp(session),session.getId(),interval, count," + 
						getMaxCountPerSecond());
				return false;
			}
			startTime=session.getLastReadTime();
			count=0;
			session.setAttribute(START_TIME, startTime);
		}
		count++;
		session.setAttribute(RECEIVE_COUNT,count);
		return true;
	}

	public int getMaxCountPerSecond() {
		return maxCountPerSecond;
	}

	public void setMaxCountPerSecond(int maxCountPerSecond) {
		this.maxCountPerSecond = maxCountPerSecond;
	}
	
}
