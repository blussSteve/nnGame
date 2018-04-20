package com.lhyone.nn.enums;

public enum NnTimeTaskEnum {
	
	USER_REDAY(1,"准备"),
	GRAB_LANDLORD(2,"抢庄"),
	FARMER_DOUBLEX(3,"闲家加倍"),
	SHOW_CARD_RESULT(4,"明牌操作"),
	SHOW_CARD_RESULT_REDAY(5,"名牌准备"),
	USER_MATCH_END_REDAY(6,"比赛结束后准备"),
	LISTEN_TIME(7,"时间监听器"),
	UNDEINDED(99,"未定义");
	private int code;
	private String desc;
	
	private NnTimeTaskEnum(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	
	
	
	
}
