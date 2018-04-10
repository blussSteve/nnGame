package com.lhyone.nn.vo;

import com.lhyone.nn.entity.GoldRecord;
import com.lhyone.nn.entity.NnRoomMatchUser;
import com.lhyone.nn.entity.UserCostRecord;
import com.lhyone.nn.entity.UserMatchRecord;

public class DbVo {
	
	private long userId;
	private int winGold;
	private GoldRecord goldRecord;
	private UserCostRecord userCostRecord;
	private NnRoomMatchUser nnRoomMatchUser;
	private UserMatchRecord userMatchRecord;
	
	
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getWinGold() {
		return winGold;
	}
	public void setWinGold(int winGold) {
		this.winGold = winGold;
	}
	public GoldRecord getGoldRecord() {
		return goldRecord;
	}
	public void setGoldRecord(GoldRecord goldRecord) {
		this.goldRecord = goldRecord;
	}
	public UserCostRecord getUserCostRecord() {
		return userCostRecord;
	}
	public void setUserCostRecord(UserCostRecord userCostRecord) {
		this.userCostRecord = userCostRecord;
	}
	public NnRoomMatchUser getNnRoomMatchUser() {
		return nnRoomMatchUser;
	}
	public void setNnRoomMatchUser(NnRoomMatchUser nnRoomMatchUser) {
		this.nnRoomMatchUser = nnRoomMatchUser;
	}
	public UserMatchRecord getUserMatchRecord() {
		return userMatchRecord;
	}
	public void setUserMatchRecord(UserMatchRecord userMatchRecord) {
		this.userMatchRecord = userMatchRecord;
	}
	
}
