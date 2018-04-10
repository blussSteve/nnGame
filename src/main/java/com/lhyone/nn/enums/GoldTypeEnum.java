package com.lhyone.nn.enums;

public enum GoldTypeEnum {
	
	PAY_CHARGE(1,"充值"),
	NIU_NIU_COST(2,"牛牛服务费"),
	HUNDRED_NIU_COST(3,"百人牛牛"),
	ZHA_JIN_HUA(4,"诈金花");
	
	private int type;
	private String desc;
	
	private GoldTypeEnum(int type, String desc) {
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
