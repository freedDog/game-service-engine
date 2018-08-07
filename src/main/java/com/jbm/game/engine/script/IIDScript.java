package com.jbm.game.engine.script;

/**
 * 有ID的脚本
 * @author JiangBangMing
 *
 * 2018年7月4日 下午2:50:01
 */
public interface IIDScript extends IScript{

	/**
	 * 
	 * @return 脚本ID,一般用于处理特殊的逻辑，策划配置的ID
	 */
	int getModelID();
}
