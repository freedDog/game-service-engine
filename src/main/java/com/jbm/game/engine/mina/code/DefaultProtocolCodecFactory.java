package com.jbm.game.engine.mina.code;

/**
 * 默认消息解析工厂
 * @author JiangBangMing
 *
 * 2018年7月5日 下午1:00:40
 */
public class DefaultProtocolCodecFactory extends ProtocolCodecFactoryImpl{

	public DefaultProtocolCodecFactory() {
		super(new ProtocolDecoderImpl(),new ProtocolEncoderImpl());
	}

}
