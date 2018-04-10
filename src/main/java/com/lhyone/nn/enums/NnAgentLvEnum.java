package com.lhyone.nn.enums;

public enum NnAgentLvEnum {

	$1(1,20,"级别一"),
	$2(2,15,"级别二"),
	$3(3,10,"级别三");
	
	private int level;
	private int rate;
	private String desc;
	
	private NnAgentLvEnum(int level, int rate, String desc) {
		this.level = level;
		this.rate = rate;
		this.desc = desc;
	}

	public int getLevel() {
		return level;
	}

	public int getRate() {
		return rate;
	}

	public String getDesc() {
		return desc;
	}
	
	
}
