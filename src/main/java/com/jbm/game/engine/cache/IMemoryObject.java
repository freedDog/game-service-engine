package com.jbm.game.engine.cache;

/**
 * 对象池对象
 * @author JiangBangMing
 *
 * 2018年7月3日 下午2:40:12
 */
public interface IMemoryObject {

	/**
	 * 对象释放并重置
	 */
	public void reset();
}
