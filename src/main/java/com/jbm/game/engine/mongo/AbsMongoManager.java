package com.jbm.game.engine.mongo;

import org.mongodb.morphia.Morphia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jbm.game.engine.util.FileUtil;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

/**
 * mongodb 管理类
 * @author JiangBangMing
 *
 * 2018年7月12日 下午6:43:46
 */
public abstract class AbsMongoManager {

	private static final Logger logger=LoggerFactory.getLogger(AbsMongoManager.class);
	
	private MongoClient mongoClient=null;
	private Morphia morphia=null;
	private MongoClientConfig mongoClientConfig;
	
	public void createConnect(String configPath) {
		mongoClientConfig=FileUtil.getConfigXML(configPath, "mongoClientConfig.xml", MongoClientConfig.class);
		if(mongoClientConfig==null) {
			throw new RuntimeException(String.format("mongodb 配置文件 %s/MongoClientConfig.xml 未找到",configPath));
		}
		
		MongoClientURI uri=new MongoClientURI(mongoClientConfig.getUrl());
		mongoClient=new MongoClient(uri);
		morphia=new Morphia();
		morphia.mapPackage("");
		
		initDao();
	}
	
	
	
	public MongoClient getMongoClient() {
		return mongoClient;
	}



	public Morphia getMorphia() {
		return morphia;
	}



	public MongoClientConfig getMongoClientConfig() {
		return mongoClientConfig;
	}



	/**
	 * 初始化dao
	 */
	protected abstract void initDao();
}
