package com.jbm.game.engine.struct.json;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 属于的set ,get方法对象封装
 * @author JiangBangMing
 *
 * 2018年7月13日 下午1:23:52
 */
public class FieldMethod {

	private final Method setmethod;
	private final Method getmethod;
	private final Field field;
	
	public FieldMethod(Method getmethod,Method setmethod,Field field) {
		this.getmethod=getmethod;
		this.field=field;
		this.setmethod=setmethod;
	}
	
	public String getName() {
		return this.field==null?"null":this.field.getName();
	}

	public Method getSetmethod() {
		return setmethod;
	}

	public Method getGetmethod() {
		return getmethod;
	}

	public Field getField() {
		return field;
	}
	
	
}
