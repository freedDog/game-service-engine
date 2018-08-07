package com.jbm.game.engine.server;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;

import com.jbm.game.engine.script.IScript;

/**
 * 服务器脚本
 * @author JiangBangMing
 *
 * 2018年7月13日 下午7:38:40
 */
public interface IServerScript extends IScript{

	/**
	 * mina 添加过滤器
	 * @param chain
	 */
	public default void addFilter(DefaultIoFilterChainBuilder chain) {
		
	}
}
