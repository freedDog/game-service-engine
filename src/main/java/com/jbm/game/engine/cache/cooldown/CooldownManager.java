package com.jbm.game.engine.cache.cooldown;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jbm.game.engine.cache.MemoryPool;
import com.jbm.game.engine.struct.Person;
import com.jbm.game.engine.util.TimeUtil;

/**
 * 冷却管理类
 * @author JiangBangMing
 *
 * 2018年7月3日 下午3:54:03
 */
public class CooldownManager {

	private static final Logger logger=LoggerFactory.getLogger(CooldownManager.class);
	
	private static final int POOL_SIZE=100000;
	
	private static final CooldownManager instance=new CooldownManager();
	
	private final MemoryPool<Cooldown> cooldownPool=new MemoryPool<>(POOL_SIZE);
	
	public static CooldownManager getInstance() {
		return instance;
	}
	
	private CooldownManager() {
		
	}
	
	/**
	 * 添加冷却
	 * @param person 对象
	 * @param type	类型
	 * @param key	关键字
	 * @param delay	冷却时间
	 * @return
	 */
	public Cooldown addCooldown(Person person,String type,String key,long delay) {
		Cooldown cooldown=null;
		if(person==null) {
			return cooldown;
		}
		//初始化冷却关键字
		String cooldownKey=this.buildCooldownKey(type, key);
		if(person.getCooldowns().containsKey(cooldownKey)) {
			cooldown=person.getCooldowns().get(cooldownKey);
			cooldown.setStart(TimeUtil.currentTimeMillis());
			cooldown.setDelay(delay);
		}else {
			//初始化冷却信息
			cooldown=this.createCooldown();
			cooldown.setType(type);
			cooldown.setKey(cooldownKey);
			cooldown.setStart(TimeUtil.currentTimeMillis());
			cooldown.setDelay(delay);
			//添加lengque
			person.getCooldowns().put(cooldownKey, cooldown);
		}
		return cooldown;
	}
	/**
	 * 冷却剩余时间
	 * @param person
	 * @param type
	 * @param key
	 * @return
	 */
	public long getCooldownTime(Person person,String type,String key) {
		Cooldown cooldown=this.getCooldown(person, type, key);
		if(cooldown!=null) {
			return cooldown.getRemainTime();
		}
		return 0;
	}
	/**
	 * 冷却结束时间
	 * @param person
	 * @param type
	 * @param key
	 * @return
	 */
	public long getCoolEndTime(Person person,String type,String key) {
		Cooldown cooldown=this.getCooldown(person, type, key);
		if(cooldown!=null) {
			return cooldown.getEndTime();
		}
		return 0;
	}
	
	/**
	 * 移除冷却
	 * @param person
	 * @param type
	 * @param key
	 */
	public void removeCooldown(Person person,String type,String key) {
		if(person==null) {
			return;
		}
		String cooldownKey=this.buildCooldownKey(type, key);
		if(person.getCooldowns().containsKey(cooldownKey)) {
			Cooldown cooldown= person.getCooldowns().remove(cooldownKey);
			if(cooldown!=null) {
				cooldownPool.put(cooldown);
			}
		}
	}
	
	/**
	 * 是否存在这种冷却类型
	 * @param person
	 * @param type
	 * @param key
	 * @return
	 */
	public boolean isExistCooldownType(Person person,String type,String key) {
		if(person==null) {
			return false;
		}
		String cooldownKey=this.buildCooldownKey(type, key);
		if(person.getCooldowns().containsKey(cooldownKey)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否在冷却中
	 * @param person
	 * @param type
	 * @param key
	 * @return
	 */
	public boolean isCooldowning(Person person,String type,String key) {
		return this.isCooldowning(person, type, key,0);
	}
	
	/**
	 * 是否在冷却中
	 * @param person
	 * @param type
	 * @param key
	 * @param allow   放宽时间  
	 * @return
	 */
	public boolean isCooldowning(Person person,String type,String key,int allow) {
		if(person==null) {
			return false;
		}
		String cooldownKey=this.buildCooldownKey(type, key);
		Cooldown cooldown=person.getCooldowns().get(cooldownKey);
		if(cooldown!=null) {
			return TimeUtil.currentTimeMillis()<=cooldown.getStart()+cooldown.getDelay()-allow;
		}
		
		return false;
	}
	/**
	 * 冷却对象
	 * @param person
	 * @param type
	 * @param key
	 * @return
	 */
	public Cooldown getCooldown(Person person,String type,String key) {
		if(person==null) {
			return null;
		}
		
		String cooldownKey=type;
		if(key!=null) {
			cooldownKey=type+"_"+key;
		}
		return person.getCooldowns().get(cooldownKey);
		
	}
	
	private Cooldown createCooldown() {
		try {
			return cooldownPool.get(Cooldown.class);
		}catch (Exception e) {
			logger.error("从对象次中获得缓冲冷却对象失败",e);
		}
		return null;
	}
	
	private String buildCooldownKey(String type,String key) {
		String cooldownKey=type;
		if(key!=null) {
			cooldownKey=type+"_"+key;
		}
		return cooldownKey;
	}
	
}
