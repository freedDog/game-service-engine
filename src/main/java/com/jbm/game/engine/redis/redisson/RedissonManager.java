package com.jbm.game.engine.redis.redisson;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jbm.game.engine.util.FileUtil;

/**
  * Redisson 工具
 * <p>
 * 慎用：<br>
 * <li>昂贵的通信代价，tcp通信非常频繁</li>
 * <li>额外功能通过lua字符串脚本实现，通信耗费较高</li>
 * <li>集合没有没有分布式多进程操作，最好全部加载到本地内存处理</li>
 * <li>垃圾回收非常频繁，jvm内存分配高达90%</li>
 * </p>
 * <br>
 * <p>
 * 		{@link RList} 迭代器每迭代一次都需要向redis服务器请求
 * </p>
 * 
 * @author JiangBangMing
 *
 * 2018年7月13日 下午2:01:23
 */
public class RedissonManager {

	private static final Logger logger=LoggerFactory.getLogger(RedissonManager.class);
	
	private static RedissonClusterConfig redissonClusterConfig;
	private static RedissonClient redisson;
	private RedissonManager() {
		
	}
	/**
	 * 连接到服务器
	 * @param configPath
	 */
	public static void connectRedis(String configPath) {
		if(redisson!=null) {
			logger.warn("Redission 客户端已经连接");
		}
		redissonClusterConfig=FileUtil.getConfigXML(configPath, "redissonClusterConfig.xml",RedissonClusterConfig.class);
		if(redissonClusterConfig==null) {
			logger.error("{}/redissonClusterConfig.xml 文件不存在",configPath);
			System.exit(0);
		}
		Config config=new Config();
		config.setCodec(new FastJsonCodec());
		ClusterServersConfig clusterServersConfig=config.useClusterServers();
		clusterServersConfig.setScanInterval(redissonClusterConfig.getScanInterval());//集群状态扫描间隔时间，单位是毫秒
		//可以用rediss://”来启用SSL连接
		redissonClusterConfig.getNodes().forEach(url -> clusterServersConfig.addNodeAddress(url));
		clusterServersConfig.setReadMode(redissonClusterConfig.getReadMode());
		clusterServersConfig.setSubscriptionMode(redissonClusterConfig.getSubscriptionMode());
		redisson=Redisson.create(config);
		
	}
	
	public static RedissonClient getRedissonClient() {
		return redisson;
	}
	
}
