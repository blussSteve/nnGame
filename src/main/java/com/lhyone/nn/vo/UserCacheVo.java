package com.lhyone.nn.vo;

import java.io.Serializable;

public class UserCacheVo  implements Serializable{

	private static final long serialVersionUID = -2140229778275422856L;

	private long userId;
	
	private String userName;
	
	private String headImgUrl;
	
	private long identiyCode;
	
	private String token;
	
	private long lastLoginTime;
	
	private String mark;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getHeadImgUrl() {
		return headImgUrl;
	}

	public void setHeadImgUrl(String headImgUrl) {
		this.headImgUrl = headImgUrl;
	}

	public long getIdentiyCode() {
		return identiyCode;
	}

	public void setIdentiyCode(long identiyCode) {
		this.identiyCode = identiyCode;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public long getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(long lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public String getMark() {
		return mark;
	}

	public void setMark(String mark) {
		this.mark = mark;
	}
	
	
	
	
}
