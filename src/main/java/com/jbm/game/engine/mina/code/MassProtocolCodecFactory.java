package com.jbm.game.engine.mina.code;

/**
 * 群发消息，带有id组的length+ buff length_iobuffer(消息队列)+id数组
 * @author JiangBangMing
 *
 * 2018年7月5日 下午12:58:25
 */
public class MassProtocolCodecFactory extends ProtocolCodecFactoryImpl{

	public MassProtocolCodecFactory() {
		super(new MassProtocolDecoder(), new MassProtocolEncoder());
	}
}
