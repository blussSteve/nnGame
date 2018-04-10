package com.lhyone.nn.enums;

public enum RedisMQEnum {
	TAKE_OUT_ROOM_USER_NN_CHANNEL("TAKE_OUT_ROOM_USER_NN_CHANNEL","剔除牛牛房间用户");
	
	private String code;
	
	private String desc;

	
	private RedisMQEnum(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	
	
	
}
