package com.jbm.game.engine.mina.code;

/**
 * 客户端消息解码
 * @author JiangBangMing
 *
 * 2018年7月5日 下午8:25:54
 */
public class ClientProtocolCodecFactory extends ProtocolCodecFactoryImpl{

	public ClientProtocolCodecFactory() {
		super(new ClientProtocolDecoder(), new ClientProtocolEncoder());
		//待发送的数据量过低，关闭当前连接
		encoder.overScheduledWriteBytesHandler=io ->{
			io.closeNow();
			return true;
		};
	}
}
