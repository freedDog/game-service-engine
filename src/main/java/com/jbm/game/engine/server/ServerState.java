package com.jbm.game.engine.server;

/**
 * 服务器状态
 * @author JiangBangMing
 *
 * 2018年7月13日 下午7:40:32
 */
public enum ServerState {

	/**正常*/
	NORMAL(0),
	/**维护*/
	MAINTAIN(1),
	;
	
	private int state;
	private ServerState(int state) {
		this.state=state;
	}
	
	public static ServerState valueOf(int state) {
		for(ServerState serverState:ServerState.values()) {
			if(state==serverState.getState()) {
				return serverState;
			}
		}
		return null;
	}
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	
	
}
