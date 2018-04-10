package com.lhyone.nn.enums;

public enum GameTypeEnum {
	NIU_NIU(1,"牛牛服务费"),
	HUNDRED_NIU(2,"百人牛牛"),
	ZHA_JIN_HUA(3,"诈金花");
	
	private int type;
	private String desc;
	
	private GameTypeEnum(int type, String desc) {
		this.type = type;
		this.desc = desc;
	}
	public int getType() {
		return type;
	}
	public String getDesc() {
		return desc;
	}
	
	
}
