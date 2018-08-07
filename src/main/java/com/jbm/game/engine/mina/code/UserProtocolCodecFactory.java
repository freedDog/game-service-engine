package com.jbm.game.engine.mina.code;

/**
 * 
 * @author JiangBangMing
 *
 * 2018年7月5日 下午8:36:14
 */
public class UserProtocolCodecFactory extends ProtocolCodecFactoryImpl{

	
	public UserProtocolCodecFactory() {
		super(new UserProtocolDecoder(),new ProtocolEncoderImpl());
		encoder.overScheduledWriteBytesHandler=io->{
			io.closeNow();
			return true;
		};
	}
	
	public void setMaxCountPerSecond(int maxCountPerSecond) {
		((UserProtocolDecoder)getDecoder()).setMaxCountPerSecond(maxCountPerSecond);
	}
}
