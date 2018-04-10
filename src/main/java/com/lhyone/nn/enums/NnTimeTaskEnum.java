package com.lhyone.nn.enums;

public enum NnTimeTaskEnum {
	
	USER_REDAY(0,"准备"),
	GRAB_LANDLORD(1,"抢庄"),
	FARMER_DOUBLEX(2,"闲家加倍"),
	SHOW_CARD_RESULT(3,"明牌操作"),
	SHOW_CARD_RESULT_REDAY(4,"名牌准备");
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
