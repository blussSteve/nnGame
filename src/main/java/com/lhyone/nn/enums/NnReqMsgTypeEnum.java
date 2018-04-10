package com.lhyone.nn.enums;

public enum NnReqMsgTypeEnum {
	INIT_ROOM(99,"房间初始化"),
	JOIN_ROOM(1,"加入房间"),
	REDAY(2,"准备"),
	ROBBERY_LANDLORD(3,"抢庄"),
	ADD_MULTIPLE(4,"闲家增加倍数"),
	SHOW_MATCH_RESULT(5,"明牌"),
	EXIT_ROOM(6,"退出房间"),
	SEND_MSG(7,"发送聊天信息"),
	SEND_APPOINT_VOICE(8,"发送指定语音"),
	SEND_VOICE(9,"发送语音"),
	SEND_EMOJI(10,"聊天表情"),
	SEND_PROP(10,"赠送礼物");
	
	private int code;
	
	private String desc;

	private NnReqMsgTypeEnum(int code, String desc) {
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
