package com.lhyone.nn.enums;

public enum NnTalkTypeEnum {
	
	TEXT(1,"文本"),
	APPOINT_VOICE(2,"指定语音"),
	VOICE(3,"语音"),
	EMOJI(4,"表情");
	
	private int code;
	private String desc;
	
	private NnTalkTypeEnum(int code, String desc) {
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
