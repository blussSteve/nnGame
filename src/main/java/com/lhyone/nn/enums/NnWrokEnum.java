package com.lhyone.nn.enums;

public enum NnWrokEnum {

	SHOW_MATCH_RESULT(1,"展示比赛结果"),
	SEND_CARD_RESULT(2,"发牌"),
	SEND_LAST_CARD_RESULT(3,"推送最后一张牌");
	private int code;
	
	private String desc;

	private NnWrokEnum(int code, String desc) {
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
