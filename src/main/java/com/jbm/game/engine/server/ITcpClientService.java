package com.jbm.game.engine.server;

/**
 * 客户端接口
 * @author JiangBangMing
 *
 * 2018年7月9日 下午2:50:12
 */
public interface ITcpClientService<T extends BaseServerConfig> extends Runnable {

	/**
	 * 发送消息
	 * @param object
	 * @return
	 */
	public boolean sendMsg(Object object);
	
	/**
	 * 检测服务器状态
	 */
	public void checkStatus();
}
