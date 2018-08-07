package com.jbm.game.engine.script;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jbm.game.engine.handler.HandlerEntity;
import com.jbm.game.engine.handler.HttpHandler;
import com.jbm.game.engine.handler.IHandler;
import com.jbm.game.engine.handler.TcpHandler;
import com.jbm.game.engine.util.FileUtil;
import com.jbm.game.engine.util.StringUtils;

/**
 * 脚本加载管理容器
 * <br>
 * 服务定位器模式
 * @author JiangBangMing
 *
 * 2018年7月4日 下午2:58:23
 */
public final class ScriptPool {

	private static final Logger logger=LoggerFactory.getLogger(ScriptPool.class);
	
	//源文件夹
	private String sourceDir;
	
	//输出文件夹
	private String outDir;
	
	//附加的jar包地址
	private String jarsDir;
	
	//脚本容器
	Map<String, Map<String,IScript>> scriptInstances=new ConcurrentHashMap<>(0);
	Map<String, Map<String,IScript>> tempScriptInstances=new ConcurrentHashMap<>(0);
	Map<Integer,IIDScript> idScriptInstances=new ConcurrentHashMap<>();
	Map<Integer, IIDScript> tempIdScriptInstancess=new ConcurrentHashMap<>();
	
	//tcp handler 容器
	Map<Integer,Class<? extends IHandler>> tcpHandlerMap=new ConcurrentHashMap<>(0);
	Map<Integer, HandlerEntity> tcpHandlerEntityMap=new ConcurrentHashMap<>(0);
	
	//http handler 容器
	Map<String, Class<? extends IHandler>> httpHandlerMap=new ConcurrentHashMap<>(0);
	Map<String, HandlerEntity> httpHandlerEntityMap=new ConcurrentHashMap<>(0);

	public ScriptPool() {

	}
	
	/**
	 * 设置编译脚本属性
	 * @param source  java 脚本路径
	 * @param out	class 编译类路径
	 * @param jarsDir 依赖jar 包路径
	 * @throws Exception
	 */
	public void setSource(String source,String out,String jarsDir) throws Exception{
		if(StringUtils.stringIsNullEmpty(source)) {
			logger.error("指定 输入 输出目录为空");
			throw new Exception("目录为空");
		}
		
		this.sourceDir=source;
		this.outDir=out;
		this.jarsDir=jarsDir;
		logger.info("脚本指定输入{} 输出{} jars目录 {}",source,out,jarsDir);
	}
	
	/**
	 * 根骨模板id获取脚本实例
	 * @param modelID
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends IIDScript> T getIIDScript(Integer modelID) {
		IIDScript iis=null;
		if(idScriptInstances.containsKey(modelID)) {
			iis=idScriptInstances.get(modelID);
		}
		return (T)iis;
	}
	
	/**
	 * 脚本列表
	 * @param name  脚本名
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <E> Collection<E> getEvts(String name){
		Map<String, IScript> scripts=ScriptPool.this.scriptInstances.get(name);
		if(scripts!=null) {
			return (Collection<E>)scripts.values();
		}
		return new ArrayList<>();
	}
	
	/**
	 * 脚本列表
	 * @param clazz 脚本类
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <E> Collection<E> getEvts(Class<E> clazz){
		Map<String, IScript> scripts=ScriptPool.this.scriptInstances.get(clazz.getName());
		if(scripts!=null) {
			return (Collection<E>)scripts.values();
		}
		return new ArrayList<>();
	}
	
	/**
	 * 执行脚本
	 * @param scriptClass  脚本类
	 * @param action 调用的方法
	 */
	@SuppressWarnings("unchecked")
	public <T extends IScript> void executeScripts(Class<T> scriptClass,Consumer<T> action) {
		Collection<IScript> evts=getEvts(scriptClass.getName());
		if(evts!=null&&!evts.isEmpty()&&action!=null) {
			evts.forEach(scrpit -> {
				try {
					action.accept((T)scrpit);
				}catch (Exception e) {
					logger.error("执行 IScript:"+scriptClass.getName(),e);
				}
			});
		}
	}
	
	/**
	 * 执行脚本，当执行结果为true时，中断执行，并返回true,否则统一返回执行 false
	 * @param scriptClass 脚本类
	 * @param condition 执行的方法
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends IScript> boolean predicateScripts(Class<? extends IScript> scriptClass,Predicate<T> condition) {
		Collection<IScript> evts=getEvts(scriptClass.getName());
		if(evts!=null&&!evts.isEmpty()&&condition!=null) {
			Iterator<IScript> iterator=evts.iterator();
			while(iterator.hasNext()) {
				try {
					if(condition.test((T)iterator.next())) {
						return true;
					}
				}catch (Exception e) {
					logger.error("predicateScripts IScript:"+scriptClass.getName(),e);
				}
			}
		}
		return false;
	}
	
	/**
	 * 执行脚本，并返回一个值
	 * @param scriptClass 脚本类
	 * @param function 执行的方法
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends IScript,R> R functionScripts(Class<? extends IScript> scriptClass,Function<T, R> function) {
		Collection<IScript> evts=getEvts(scriptClass.getName());
		if(evts!=null&&!evts.isEmpty()&&function!=null) {
			Iterator<IScript> iterator=evts.iterator();
			while(iterator.hasNext()) {
				try {
					R r=function.apply((T)iterator.next());
					if(r!=null) {
						return r;
					}
				}catch (Exception e) {
					logger.error("functionScripts IBaseScript: "+scriptClass.getName(),e);
				}
			}
		}
		return null;
	} 
	
	/**
	 * 编译java源文件
	 * @return
	 */
	public String compile() {
		FileUtil.deleteDirectory(this.outDir);//删除之前的class文件
		List<File> sourceFileList=new ArrayList<>();
		FileUtil.getFiles(this.sourceDir, sourceFileList, ".java", null);//获取源文件
		return this.compile(sourceFileList);
	}
	
	/**
	 * 加载脚本文件
	 * @param sourceFileList 文件列表
	 * @return 编译错误信息
	 */
	public String compile(List<File> sourceFileList) {
		StringBuilder sb=new StringBuilder();
		if(sourceFileList!=null) {
			DiagnosticCollector<JavaFileObject> diagnosticCollector=new DiagnosticCollector<>();
			//获取编译器实例
			JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
			//获取标准文件管理实例
			StandardJavaFileManager fileManager=compiler.getStandardFileManager(diagnosticCollector, null, Charset.forName("utf-8"));
			try {
				//没有java文件 ，直接返回
				if(sourceFileList.isEmpty()) {
					return this.sourceDir+" 目录下查找不到任何java文件";
				}
				logger.info("找到脚本并且需要编译的文件共:"+sourceFileList.size());
				//创建输出目录，如果不存在话
				new File(this.outDir).mkdirs();
				//获取要编译的编译单元
				Iterable<? extends JavaFileObject> compilationUnits=fileManager.getJavaFileObjectsFromFiles(sourceFileList);
				/**
				 * 编译选项，在编译程序会自动的去寻找java文件引用的其他java源文件或者class.
				 * -sourcepath 选项就是定义java源文件的查找目录，-classpath选项就是定义class文件的查找目录
				 */
				ArrayList<String> options=new ArrayList<>(0);
				options.add("-g");
				options.add("-source");
				options.add("1.8");
				options.add("-encoding");
				options.add("UTF-8");
				options.add("-sourcepath");
				options.add(this.sourceDir);//指定文件目录
				options.add("-d");
				options.add(this.outDir);//指定输出目录
				
				ArrayList<File> jarsList=new ArrayList<>();
				FileUtil.getFiles(this.jarsDir, jarsList, ".jar", null);
				String jarString="";
				jarString=jarsList.stream().map((jar) ->jar.getPath()+File.pathSeparator).reduce(jarString, String::concat);
				logger.info("jarString :"+jarString);
				
				if(!StringUtils.stringIsNullEmpty(jarString)) {
					options.add("-classpath");
					options.add(jarString);
				}
				
				JavaCompiler.CompilationTask compilationTask=compiler.getTask(null, fileManager, diagnosticCollector, options, null, compilationUnits);
				//运行编译任务
				Boolean call=compilationTask.call();
				if(!call) {
					diagnosticCollector.getDiagnostics().forEach(f ->{
						sb.append(";").append(((JavaFileObject)(f.getSource())).getName()).append(" line:")
						.append(f.getLineNumber());
						logger.warn("加载脚本错误:"+((JavaFileObject)(f.getSource())).getName()+"  line:"+f.getLineNumber());
					});
				}
			}catch (Exception e) {
				sb.append(this.sourceDir).append("错误： ").append(e);
				logger.error("加载脚本错误: ",e);
			}finally {
				try {
					fileManager.close();
				}catch (IOException e) {
					logger.error(""+e);
				}
			}
		}else {
			logger.warn(this.sourceDir+"目录下查找不到任何java文件");
		}
		return sb.toString();
	}
	
	/**
	 * 加载脚本文件
	 * @param condition 输出条件
	 * @return
	 */
	public String loadJava(Consumer<String> condition) {
		String compile=this.compile();
		StringBuilder sb=new StringBuilder();
		if(compile==null||compile.isEmpty()) {
			List<File> sourceFileList=new ArrayList<>(0);
			//得到编译后的class文件
			FileUtil.getFiles(this.outDir, sourceFileList, ".class", null);
			String[] fileNames=new String[sourceFileList.size()];//类路径列表
			for(int i=0;i<sourceFileList.size();i++) {
				fileNames[i]=sourceFileList.get(i).getPath();
				sb.append(fileNames[i]).append(":");
			}
			tempScriptInstances=new ConcurrentHashMap<>();
			tempIdScriptInstancess=new ConcurrentHashMap<>();
			loadClass(fileNames);
			if(tempScriptInstances.size()>0) {
				scriptInstances.clear();
				scriptInstances=tempScriptInstances;
			}
			if(tempIdScriptInstancess.size()>0) {
				idScriptInstances.clear();
				idScriptInstances=tempIdScriptInstancess;
			}
		}else {
			if(!compile.isEmpty()) {
				if(condition!=null) {
					condition.accept(compile);
				}
			}
		}
		return sb.toString();
	}
	/**
	 *加载脚本文件
	 * @param source 加载的文件或者目录
	 * @return
	 */
	public String loadJava(String... source) {
		FileUtil.deleteDirectory(this.outDir);
		List<File> sourceFileList=new ArrayList<>();
		FileUtil.getFiles(this.sourceDir, sourceFileList, ".java", fileAbsolutePath->{
			if(source==null) {
				return true;
			}
			for(String str:source) {
				if(fileAbsolutePath.contains(str)||str.equals("")) {
					return true;
				}
			}
			return false;
		});
		
		String result=this.compile(sourceFileList);
		StringBuilder loadJava=new StringBuilder();
		if(result==null||result.isEmpty()) {
			sourceFileList.clear();
			FileUtil.getFiles(this.outDir, sourceFileList, ".class", fileAbsolutePath->{
				if(source==null) {
					return true;
				}
				for(String str:source) {
					if(fileAbsolutePath.contains(str)||str.equals("")) {
						return true;
					}
				}
				return false;
			});
			String[] fileNames=new String[sourceFileList.size()];
			for(int i=0;i<sourceFileList.size();i++) {
				fileNames[i]=sourceFileList.get(i).getPath();
				loadJava.append(fileNames[i]).append(";/r/n");
			}
			tempScriptInstances=new ConcurrentHashMap<>();
			tempIdScriptInstancess=new ConcurrentHashMap<>();
			loadClass(fileNames);
			if(tempScriptInstances.size()>0){
				tempScriptInstances.entrySet().stream().forEachOrdered((entry) ->{
					String key=entry.getKey();
					Map<String, IScript> value=entry.getValue();
					scriptInstances.put(key, value);
				});
			}
			if(tempIdScriptInstancess.size()>0) {
				tempIdScriptInstancess.entrySet().stream().forEach((entry) ->{
					Integer key=entry.getKey();
					IIDScript value=entry.getValue();
					idScriptInstances.put(key, value);
				});
			}
		}
		return loadJava.toString();
	}
	
	/**
	 * 添加handler
	 * @param clazz
	 */
	public void addHandler(Class<? extends IHandler> clazz) {
		if(IHandler.class.isAssignableFrom(clazz)) {
			HandlerEntity handlerEntity=clazz.getAnnotation(HandlerEntity.class);
			if(handlerEntity!=null) {
				if(TcpHandler.class.isAssignableFrom(clazz)) {
					tcpHandlerMap.put(handlerEntity.mid(), (Class<? extends IHandler>) clazz);
					tcpHandlerEntityMap.put(handlerEntity.mid(), handlerEntity);
					logger.info("[{}] 加载到 tcp handler 容器",clazz.getName());
				}else if(HttpHandler.class.isAssignableFrom(clazz)) {
					httpHandlerMap.put(handlerEntity.path(), (Class<? extends IHandler>)clazz);
					httpHandlerEntityMap.put(handlerEntity.path(), handlerEntity);
					logger.info("[{}] 加载到http handler 容器",clazz.getName());
				}else {
					logger.warn("handler[{}] 未继承 handler ",clazz.getName());
				}
			}else {
				logger.warn("handler [{}] 未添加注解",clazz.getName());
			}
		}
	}
	
	public Map<Integer, Class<? extends IHandler>> getHandlerMap(){
		return tcpHandlerMap;
	}
	
	public Map<Integer, HandlerEntity> getHandlerEntityMap(){
		return tcpHandlerEntityMap;
	}
	
	public Map<String, Class<? extends IHandler>> getHttpHandlerMap(){
		return httpHandlerMap;
	}
	
	public Map<String, HandlerEntity> getHttpHandlerEntityMap(){
		return httpHandlerEntityMap;
	}
	
	/**
	 * 加载脚本文件
	 * @param names
	 */
	private void loadClass(String... names) {
		try {
			ScriptClassLoader loader=new ScriptClassLoader();
			for(String name:names) {
				String tempName=name.replace(outDir, "").replace(".class", "").replace(File.separatorChar, '.');
				loader.loadClass(tempName);
			}
		}catch (ClassNotFoundException e) {
			logger.error("",e);
		}
	}
	/**
	 * 
	 * @author JiangBangMing
	 *
	 * 2018年7月4日 下午4:51:15
	 */
	private class ScriptClassLoader extends ClassLoader{
		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			Class<?> defineClass=null;
			defineClass=super.loadClass(name);
			return defineClass;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			logger.info("加载脚本目录名称:"+name);
			byte[] classData=getClassData(name);
			Class<?> defineClass=null;
			if(classData!=null) {
				try {
					defineClass=defineClass(name, classData, 0,classData.length);
					String nameString=defineClass.getName();
					if(!Modifier.isAbstract(defineClass.getModifiers())
							&&!Modifier.isPrivate(defineClass.getModifiers())
							&&!Modifier.isStatic(defineClass.getModifiers())
							&&!nameString.contains("$")) {
						Object newInstance=defineClass.newInstance();
						List<Class<?>> interfaces=new ArrayList<>();//实现的接口
						if(IInitScript.class.isAssignableFrom(defineClass)
								||IScript.class.isAssignableFrom(defineClass)) {
							Class<?> cls=defineClass;
							while(cls!=null&&!cls.isInterface()&&!cls.isPrimitive()) {
								interfaces.addAll(Arrays.asList(cls.getInterfaces()));
								cls=cls.getSuperclass();
							}
							if(newInstance instanceof IInitScript) {//执行初始方法
								((IInitScript)newInstance).init();
							}
						}
						//脚本
						if(newInstance!=null&&!interfaces.isEmpty()) {
							for(Class<?> aInterface:interfaces) {
								if(IScript.class.isAssignableFrom(aInterface)) {
									if(!tempScriptInstances.containsKey(aInterface.getName())) {
										tempScriptInstances.put(aInterface.getName(), new ConcurrentHashMap<>());
									}
									logger.info("脚本{} 加载到IScript容器",nameString);
									tempScriptInstances.get(aInterface.getName()).put(defineClass.getName(),(IScript)newInstance);
								}
								if(IIDScript.class.isAssignableFrom(aInterface)) {
									logger.info("脚本{} 加载到IIDScript容器:",nameString);
									IIDScript iis=(IIDScript)newInstance;
									tempIdScriptInstancess.put(iis.getModelID(), iis);
								}
							}
						}
						
						//handler
						if(IHandler.class.isAssignableFrom(defineClass)) {
							HandlerEntity handlerEntity=defineClass.getAnnotation(HandlerEntity.class);
							if(handlerEntity!=null) {
								if(TcpHandler.class.isAssignableFrom(defineClass)) {
									if(defineClass.getName().contains("ServerRegisterResHandler")) {
										System.out.print("");
									}
									tcpHandlerMap.put(handlerEntity.mid(), (Class<? extends IHandler>)defineClass);
									tcpHandlerEntityMap.put(handlerEntity.mid(), handlerEntity);
									logger.info("[{}]加载到tcp handler 容器",nameString);
								}else if(HttpHandler.class.isAssignableFrom(defineClass)) {
									httpHandlerMap.put(handlerEntity.path(), (Class<? extends IHandler>)defineClass);
									httpHandlerEntityMap.put(handlerEntity.path(), handlerEntity);
									logger.info("[{}] 加载到http handler 容器",nameString);
								}else {
									logger.warn("handler [{}]未继承Handler",defineClass.getSimpleName());
								}
							}else {
								logger.warn("handler [{}]没添加注解",defineClass.getSimpleName());
							}
						}
					}else {
						logger.warn("handler[{}] 么有加载脚本:"+nameString);
					}
				}catch (Exception e) {
					logger.error("加载脚本发生错误",e);
				}
			}
			return defineClass;
		}
		private byte[] getClassData(String className) {
			String path=classNameToPath(className);
			logger.info("加载脚本路径:"+path);
			InputStream ins=null;
			try {
				File file=new File(path);
				if(file.exists()) {
					ins=new FileInputStream(path);
					ByteArrayOutputStream baos=new ByteArrayOutputStream();
					int bufferSize=4096;
					byte[] buffer=new byte[bufferSize];
					int bytesNumRead=0;
					while((bytesNumRead=ins.read(buffer))!=-1) {
						baos.write(buffer,0,bytesNumRead);
					}
					return baos.toByteArray();
				}else {
					logger.error("自定脚本文件不存在:"+path);
				}
			}catch (Exception e) {
				logger.error("",e);
			}finally {
				if(ins!=null) {
					try {
						ins.close();
					}catch (Exception e) {
						logger.error("",e);
					}
				}
			}
			return null;
		}
		
		private String classNameToPath(String className) {
			File file=null;
			try {
				String path=outDir+className.replace('.', File.separatorChar)+".class";
				logger.info("classNameToPath path{}",path);
				file=new File(path);
				if(!file.exists()) {
					logger.warn("classNameToPath path:{}不存在",path);
				}
				return file.getPath();
			}catch (Exception e) {
				logger.error(outDir,e);
			}
			return "";
		}
	}
}
