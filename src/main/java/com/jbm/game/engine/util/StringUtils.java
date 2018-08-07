package com.jbm.game.engine.util;

public class StringUtils {
	/**
	 * 字符串为空字符串
	 * 
	 * @param str
	 * @return
	 */
	public static final boolean stringIsNullEmpty(String str) {
		return str == null || str.length() <= 0 || "".equals(str.trim());
	}

}
