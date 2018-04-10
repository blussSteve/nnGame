package com.lhyone.nn.vo;

public class NnRoomUserTalkVo {

	private long userId;
	private long roomId;
	private String roomNo;
	//聊天类型[1.文字 2.指定语音 3.语音 4. 表情]
	private int talkType;
	private String msg;
	private int voiceType;
	private byte[] voice;
	private long createDate;
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	
	
	public long getRoomId() {
		return roomId;
	}
	public void setRoomId(long roomId) {
		this.roomId = roomId;
	}
	public String getRoomNo() {
		return roomNo;
	}
	public void setRoomNo(String roomNo) {
		this.roomNo = roomNo;
	}
	public int getTalkType() {
		return talkType;
	}
	public void setTalkType(int talkType) {
		this.talkType = talkType;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public int getVoiceType() {
		return voiceType;
	}
	public void setVoiceType(int voiceType) {
		this.voiceType = voiceType;
	}
	
	public byte[] getVoice() {
		return voice;
	}
	public void setVoice(byte[] voice) {
		this.voice = voice;
	}
	public long getCreateDate() {
		return createDate;
	}
	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}
	
	
}
