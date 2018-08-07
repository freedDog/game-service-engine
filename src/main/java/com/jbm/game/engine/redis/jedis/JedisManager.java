package com.jbm.game.engine.redis.jedis;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jbm.game.engine.util.FileUtil;
import com.jbm.game.engine.util.JsonUtil;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

/**
 * redis 集群管理类
 * @author JiangBangMing
 *
 * 2018年7月13日 下午12:36:33
 */
public class JedisManager {

	private static final Logger logger=LoggerFactory.getLogger(JedisManager.class);
	private static JedisCluster jedisCluster;
	private static JedisManager jedisManager;
	private Map<String, String> keysShaMap;//key 脚本名称
	
	/**
	 * 
	 * @param configPath  redis 配置文件路径
	 */
	public JedisManager(String configPath) {
		this(loadJedisClusterConfig(configPath));
	}
	
	public JedisManager(JedisClusterConfig config) {
		HashSet<HostAndPort> jedisClusterNodes=new HashSet<>();
		config.getNodes().forEach( node ->{
			if(node==null) {
				return;
			}
			try {
				if(node.getIp()!=null&&node.getIp().length()>5) {
					jedisClusterNodes.add(new HostAndPort(node.getIp(), node.getPort()));
				}
			}catch (Exception e) {
				logger.error(node.toString(),e);
			}
		});
		GenericObjectPoolConfig poolConfig=new GenericObjectPoolConfig();
		poolConfig.setMaxTotal(config.getPoolMaxTotal());
		poolConfig.setMaxIdle(config.getPoolMaxIdle());
		poolConfig.setMaxWaitMillis(config.getMaxWaitMillis());
		poolConfig.setTimeBetweenEvictionRunsMillis(config.getTimeBetweenEvictionRunsMillis());
		poolConfig.setMinEvictableIdleTimeMillis(config.getMinEvictableIdleTimeMillis());
		poolConfig.setSoftMinEvictableIdleTimeMillis(config.getSoftMinEvictableIdleTimeMillis());
		poolConfig.setTestOnBorrow(config.isTestOnBorrow());
		poolConfig.setTestWhileIdle(config.isTestWhileIdle());
		poolConfig.setTestOnReturn(config.isTestOnReturn());
		jedisCluster=new JedisCluster(jedisClusterNodes,config.getConnectionTimeout(),config.getSoTimeout(),
				config.getMaxRedirections(),poolConfig);
	}
	
	public static JedisCluster getJedisCluster() {
		return jedisCluster;
	}
	
	public static JedisManager getInstance() {
		return jedisManager;
	}
	
	public static void setReadisManager(JedisManager jedisManager) {
		JedisManager.jedisManager=jedisManager;
	}
	
	/**
	 * 初始化脚本
	 * @param configPath
	 */
	public void initScript(String configPath) {
		try {
			String path=configPath+File.separator+"lua";//lua脚本路径
			List<File> sources=new ArrayList<>();
			FileUtil.getFiles(path, sources, ".lua", null);
			if(sources.size()<1) {
				logger.warn("{} 目录无任何lua脚本");
				return;
			}
			for(File file:sources) {
				String fileName=file.getName().substring(0, file.getName().indexOf("."));
				scriptFlush(fileName);
				loadScript(path, fileName);
			}
			
		}catch (Exception e) {
			logger.error("redis 脚本",e);
		}
	}
	
	
	/**
	 * 初始化脚本
	 * @param path 脚本所在路径
	 * @param fileName  脚本文件名称
	 * @throws Exception
	 */
	public void loadScript(String path,String fileName) throws Exception{
		String script=FileUtil.readTxtFile(path+File.separator,fileName+".lua");
		if(script==null||script.length()<0) {
			throw new Exception(path+"/"+fileName+".lua 加载出错");
		}
		String hash=JedisManager.getJedisCluster().scriptLoad(script, fileName);
		if(hash==null||hash.length()<1) {
			throw new Exception(fileName+".lua 脚本注入出错");
		}
		
		if(keysShaMap==null) {
			keysShaMap=new HashMap<>();
		}
		this.keysShaMap.put(fileName, hash);
		if(logger.isDebugEnabled()) {
			logger.debug("redis 脚本:{}----{} 加载成功",fileName,hash);
		}
	}
	
	/**
	 * 清除脚本缓存
	 * @param fileName
	 */
	public void scriptFlush(String fileName) {
		JedisManager.getJedisCluster().scriptFlush(fileName.getBytes());
	}
	/**
	 * 执行脚本
	 * @param scriptName 脚本文件名称
	 * @param keys redis key列表
	 * @param args 参数集合
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T executeScript(String scriptName,List<String> keys,List<String> args) {
		String sha=getSha(scriptName);
		if(sha==null) {
			return null;
		}
		Object object=JedisManager.getJedisCluster().evalsha(sha, keys,args);
		if(object==null) {
			return null;
		}
		return (T)object;
	}
	
	/**
	 * 获取所有map 对象
	 */
	@SuppressWarnings("unchecked")
	public <K,V> Map<K, V> hgetAll(final String key,final Class<K> keyClass,final Class<V> valueClass){
		Map<String, String> hgetAll=getJedisCluster().hgetAll(key);
		if(hgetAll==null) {
			return null;
		}
		Map<K, V> map=new ConcurrentHashMap<>();
		hgetAll.forEach((k,v) -> {
			map.put((K)parseKey(k, keyClass), JsonUtil.parseObject(v, valueClass));
		});
		return map;
	}
	
	/**
	 * 获取map指定属性对象
	 * @param key
	 * @param field
	 * @param clazz
	 * @return
	 */
	public <V> V hget(final String key,final Object field,Class<V> clazz) {
		String hget=getJedisCluster().hget(key, field.toString());
		if(hget==null) {
			return null;
		}
		return JsonUtil.parseObject(hget, clazz);
	}
	
	/**
	 * 存储map 对象
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 */
	public Long hset(final String key,final Object field,final Object value) {
		return getJedisCluster().hset(key, field.toString(), JsonUtil.toJSONStringWriteClassNameWithFiled(value));
	}
	
	
	/**
	 * 加载redis 配置文件
	 * @param configPath
	 * @return
	 */
	private static JedisClusterConfig loadJedisClusterConfig(String configPath) {
		JedisClusterConfig jedisClusterConfig=FileUtil.getConfigXML(configPath, "jedisClusterConfig.xml", JedisClusterConfig.class);
		if(jedisClusterConfig==null) {
			logger.error("redis 配置{} 未找到",configPath);
			System.exit(1);
		}
		return jedisClusterConfig;
	}
	
	/**
	 * 获取脚本
	 * @param fileName
	 * @return
	 */
	private String getSha(String fileName) {
		if(keysShaMap.containsKey(fileName)) {
			return keysShaMap.get(fileName);
		}
		logger.warn(String.format("脚本 %s 没有初始化", fileName));
		return null;
	}
	/**
	 * 解析key 对象
	 * @param key
	 * @param keyClass
	 * @return
	 */
	private Object parseKey(String key,Class<?> keyClass) {
		if(keyClass.isAssignableFrom(Long.class)) {
			return Long.parseLong(key);
		}else if(keyClass.isAssignableFrom(Integer.class)) {
			return Integer.parseInt(key);
		}
		return key;
		
	}
}
