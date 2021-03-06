package com.jbm.game.engine.mq;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MQ 消息生产者，消息发送
 * @author JiangBangMing
 *
 * 2018年7月12日 下午7:18:26
 */
public class MQProducer extends MQService {
	
	private static final Logger logger=LoggerFactory.getLogger(MQProducer.class);
	
	public MQProducer(MQConfig mqConfig) {
		super(mqConfig);
	}
	
	/**
	 * 发送消息
	 * @param destName
	 * @param msg
	 */
	public void sendMsg(String destName,String msg) {
		sendMsg(destName, msg,DeliveryMode.NON_PERSISTENT);
	}
	
	/**
	 *消息进行持久化
	 * @param destName
	 * @param msg
	 */
	public void sendPresistentMsg(String destName,String msg) {
		sendMsg(destName, msg,DeliveryMode.PERSISTENT);
	}
	
	private boolean sendMsg(String destName,String msg,int deliverMode) {
		Connection conn=getConnection();
		if(conn==null) {
			logger.error("MQ 创建连接失败 消息:{}",msg);
			return false;
		}
		Session session=null;
		try {
			conn.start();
			session=conn.createSession(false,Session.CLIENT_ACKNOWLEDGE);
			Destination destination=session.createQueue(destName);
			MessageProducer producer=session.createProducer(destination);
			producer.setDeliveryMode(deliverMode);
			TextMessage message=session.createTextMessage(msg);
			producer.send(message);
			return true;
		}catch (Exception e) {
			logger.error("sendMsg",e);
			this.closeConnection();
		}finally {
			if(session!=null) {
				try {
					session.close();
				}catch (Exception e) {
					logger.error("sendMsg",e);
				}
			}
		}
		
		return false;
	}
}
