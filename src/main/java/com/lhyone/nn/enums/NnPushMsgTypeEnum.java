package com.lhyone.nn.enums;

public enum NnPushMsgTypeEnum {
	CREATE_ROOM_PUSH(100,"创建房间推送"),
	JOIN_ROOM_PUSH(101,"加入房间推送"),
	REDAY_ROOM_PUSH(102,"用户准备推送"),
	SEND_CARD_PUSH(103,"用户发牌推送"),
	GRAB_LANDLORD_PUSH(104,"抢庄"), 
	SET_LANDLORD_PUSH(105,"设置庄家"),
	FARMER_SET_SCORE_DOUBLE_PUSH(106,"闲家压分"),
	SEND_LAST_CARD_PUSH(107,"发送最后一张牌"),
	SET_SHOW_CARD_PUSH(108,"明牌"),
	SEND_MATCH_RESULT_PUSH(109,"推送比赛结果"),
	SEND_MATCH_LAST_RESULT_PUSH(110,"推送比赛最终结果"), 
	INIT_MATCH_PUSH(111,"比赛初始化"),
	APPLY_CLOSE_ROOM_PUSH(112,"申请关闭房间推送"),
	AGREE_CLOSE_ROOM_PUSH(113,"同意关闭房间推送"),
	REFUSE_CLOSE_ROOM_PUSH(114,"拒绝关闭房间推送推送"),
	EXIT_ROOM_PUSH(115,"退出房间推送"),
	SEND_MSG_PUSH(116,"发送聊天信息反馈"),
	SEND_APPOINT_VOICE_PUSH(117,"发送指定语音反馈"),
	SEND_VOICE_PUSH(118,"发送语音反馈"),
	SEND_EMOJI_PUSH(119,"发送表情反馈"),
	SEND_GOLD_NOT_ENOUGH_PUSH(120,"金币不足推送"),
	SEND_PROP_PUSH(121,"道具推送"),
	GO_RUN_GAME_PUSH(122,"继续游戏推送"),
	USER_AGREE_CLOSE_ROOM_PUSH(123,"同意解散房间推送");
	
	private int code;
	
	private String desc;

	private NnPushMsgTypeEnum(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}
	/**
	 * 是否包含该类型
	 * @param pushType
	 * @return
	 */
	public static boolean isInclude(NnPushMsgTypeEnum pushType){
		for(NnPushMsgTypeEnum enums:NnPushMsgTypeEnum.values()){
			
			if(enums==pushType){
				
				return true;
			}
		}
		return false;
		
	}
	
	public int getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
}
