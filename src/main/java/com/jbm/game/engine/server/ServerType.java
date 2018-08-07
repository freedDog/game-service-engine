package com.jbm.game.engine.server;

/**
 * 服务器类型
 * @author JiangBangMing
 *
 * 2018年7月4日 下午9:28:43
 */
public enum ServerType {
    NONE(-1),
    /**网关*/
    GATE(1),
    GAME(2),
    /**集群管理服*/
    CLUSTER(3),
    LOG(4),
    /**聊天*/
    CHAT(5),
    PAY(6),
    /**大厅*/
    HALL(7),
    /**捕鱼达人*/
    GAME_BYDR(101),
    ;
    
	public static ServerType valueof(int type) {
		for(ServerType t:ServerType.values()) {
			if(t.getType()==type) {
				return t;
			}
		}
		return NONE;
	}
	
	public int getType() {
		return type;
	}
	
    private final int type;
    
    
    
    private ServerType(int type) {
    	this.type=type;
    }
}
