package com.lhyone.nn.enums;

public enum NnRspMsgTypeEnum {
	INIT_ROOM_FEEDBACK(99,"房间初始化"),
	JOIN_ROOM_FEEDBACK(2,"加入房间反馈"),
	REDAY_ROOM_FEEDBACK(3,"准备反馈"),
	GRAB_LANDLORD_FEEDBACK(4,"抢庄反馈"),
	SET_FARMER_SCORE_FEEDBACK(5,"闲家设置底分反馈"),
	SHOW_CARD_FEEDBACK(6,"明牌反馈"),
	NEXT_MATCH_FEEDBACK(7,"开始下一局比赛"),
	APPLY_CLOSE_ROOM_FEEDBACK(8,"申请解散房间"),
	AGREE_CLOSE_ROOM_FEEDBACK(9,"同意解散房间反馈"),
	REFUSE_CLOSE_FEEDBACK(10,"拒绝解散房间反馈"),
	EXIT_ROOM_FEEDBACK(11,"退出房间反馈"),
	GO_HOME_FEEDBACK(12,"返回大厅反馈"),
	SEND_MSG_FEEDBACK(13,"发送聊天信息反馈"),
	SEND_APPOINT_VOICE_FEEDBACK(14,"发送指定语音反馈"),
	SEND_VOICE_FEEDBACK(15,"发送语音反馈"),
	SEND_EMOJI_FEEDBACK(16,"赠送表情反馈"),
	SEND_PROP_FEEDBACK(17,"发送礼物反馈"),
	GO_RUN_GAME_FEEDBACK(18,"继续游戏反馈");
	
	private int code;
	
	private String desc;

	private NnRspMsgTypeEnum(int code, String desc) {
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
