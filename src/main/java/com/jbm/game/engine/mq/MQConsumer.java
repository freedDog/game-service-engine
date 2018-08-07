package com.jbm.game.engine.mq;

import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jbm.game.engine.script.ScriptManager;

/**
 * MQ 消费者，监听器
 * @author JiangBangMing
 *
 * 2018年7月12日 下午7:07:13
 */
public class MQConsumer extends MQService implements Runnable{

	private static final Logger logger=LoggerFactory.getLogger(MQConsumer.class);
	
	private String queueName;//队列名称
	private boolean connected;//是否连接
	
	public MQConsumer(MQConfig mqConfig) {
		super(mqConfig);
		this.queueName=mqConfig.getQueueName();
	}
	
	public MQConsumer(String configPath,String queueName) {
		super(configPath);
		this.queueName=queueName;
	}
	
	@Override
	public void run() {
		MessageConsumer consumer=null;
		while(true) {
			try {
				if(!connected) {//连接
					Connection conn=getConnection();
					if(conn==null) {
						logger.error("启动 MQ失败，获取连接失败");
						this.connected=false;
						Thread.sleep(3000);
						break;
					}
					conn.start();
					Session session=conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
					consumer=session.createConsumer(new ActiveMQQueue(this.queueName));
					this.connected=true;
				}else if(consumer!=null){//接受消息
					Message msg=consumer.receive();
					if(msg==null) {
						continue;
					}
					if(msg instanceof TextMessage) {
						String body=((TextMessage)msg).getText();
						if(body==null) {
							continue;
						}
						ScriptManager.getInstance().getBaseScriptEntry().executeScripts(IMQScript.class, script -> script.onMessage(body));
					}else {
						logger.error("不支持的消息:{}",msg.getClass().getName());
					}
					
				}
			}catch (Exception e) {
				logger.error("消息接受",e);
				this.closeConnection();
				this.connected=false;
			}
		}
		
	}
	public void stop() {
		this.closeConnection();
	}
}
