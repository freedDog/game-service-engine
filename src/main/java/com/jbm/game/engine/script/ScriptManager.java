package com.jbm.game.engine.script;

import java.io.File;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jbm.game.engine.handler.HandlerEntity;
import com.jbm.game.engine.handler.IHandler;

/**
 * 脚本管理
 * @author JiangBangMing
 *
 * 2018年7月4日 下午2:57:28
 */
public class ScriptManager {
	private static final Logger logger=LoggerFactory.getLogger(ScriptManager.class);
	private static final ScriptManager instance=new ScriptManager();
	private static final ScriptPool scriptPool;//基础脚本类
	
	//静态初始化
	static {
		scriptPool=new ScriptPool();
		try {
			String property=System.getProperty("user.dir");
			String path=property+"-scripts"+File.separator+"src"+File.separator+"main"+File.separator//脚本路径
					+"java"+File.separator;
			String outpath=property+File.separator+"target"+File.separator+"scriptsbin"+File.separator;//class类编译路径
			String jarDir=property+File.separator+"target"+File.separator;//jar包路径
			scriptPool.setSource(path, outpath, jarDir);
		}catch (Exception e) {
			logger.error("",e);
		}
	}
	
	public static ScriptManager getInstance() {
		return instance;
	}
	
	public ScriptPool getBaseScriptEntry() {
		return scriptPool;
	}
	/**
	 * 初始化脚本
	 * @param result
	 * @return 加载输出结果字符
	 */
	public String init(Consumer<String> result) {
		return scriptPool.loadJava(result);
	}
	/**
	 * 加载指定实例，可以是文件也可以是目录
	 * @param source
	 * @return
	 */
	public String loadJava(String... source) {
		return scriptPool.loadJava(source);
	}
	/**
	 * 获取消息处理器
	 * @param mid
	 * @return
	 */
	public Class<? extends IHandler> getTcpHandler(int mid){
		return scriptPool.getHandlerMap().get(mid);
	}
	
	/**
	 * 获取handler 配置
	 * @param mid
	 * @return
	 */
	public HandlerEntity getTcpHandlerEntity(int mid) {
		return scriptPool.getHandlerEntityMap().get(mid);
	}
	
	/**
	 * tcp 消息是否已经注册
	 * @param mid
	 * @return
	 */
	public boolean tcpMsgIsRegister(int mid) {
		return scriptPool.getHandlerMap().containsKey(mid);
	}
	/**
	 * 获取http 消息处理器
	 * @param path
	 * @return
	 */
	public Class<? extends IHandler> getHttpHandler(String path){
		return scriptPool.getHttpHandlerMap().get(path);
	}
	
	/**
	 * 获取http handler 配置
	 * @param path
	 * @return
	 */
	public HandlerEntity getHttpHandlerEntity(String path) {
		return scriptPool.getHttpHandlerEntityMap().get(path);
	}
	/**
	 * 手动添加消息处理器
	 * 非脚本目录下的需要手动添加到容器中
	 * @param clazz
	 */
	public void addIHandler(Class<? extends IHandler> clazz) {
		scriptPool.addHandler(clazz);
	}
}
