package com.lhyone.nn.enums;

public enum NnUserRoleEnum {

	LANDLORD(1,"地主"),
	FARMER(2,"农民"),
	GUEST(3,"游客"),
	MATCH(4,"游客准备");
	
	private int code;
	private String desc;
	
	private NnUserRoleEnum(int code, String desc) {
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
