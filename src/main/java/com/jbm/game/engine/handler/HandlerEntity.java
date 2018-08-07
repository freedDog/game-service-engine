package com.jbm.game.engine.handler;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.google.protobuf.Message;
import com.jbm.game.engine.thread.ThreadType;

/**
 * 消息处理注释
 * @author JiangBangMing
 *
 * 2018年7月3日 下午7:00:35
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface HandlerEntity {

	/**
	 * tcp 消息id
	 * @return
	 */
	int mid() default 0;
	
	/**
	 * http 请求路径
	 * @return
	 */
	String path() default "";
	
	/**
	 * 描述
	 * @return
	 */
	String desc() default "";
	
	/**
	 * 调用的线程
	 * @return
	 */
	ThreadType thread() default ThreadType.IO;
	
	/**
	 * tcp 请求的消息类
	 * @return
	 */
	Class<? extends Message> msg() default Message.class;
}
