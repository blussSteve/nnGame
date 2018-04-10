package com.lhyone.nn.enums;

public enum NnRoomMatchStatusEnum {

	INIT_HOME_STATUS(1,"房间初始状态"),
	REDAY_STATUS(2,"用户准备的状态"),
	GRAB_LANDLORD_STATUS(3,"抢庄的状态"),
	FARMER_STATUS(4,"闲家选分状态"),
	PLAY_GAME_STATUS(5,"明牌状态"),
	SHOW_MATCH_RESULT_STATUS(6,"展示结果的状态"),
	SHOW_MATCH_LAST_RESULT_STATUS(7,"展示比赛最终结果"),
	CLOSE_HOME_STATUS(8,"解散房间状态");
	
	
	private int code;
	private String desc;
	
	private NnRoomMatchStatusEnum(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}
	public int getCode() {
		return code;
	}
	public String getDesc() {
		return desc;
	}
	
	
	
	
}
