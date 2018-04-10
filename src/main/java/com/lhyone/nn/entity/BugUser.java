package com.lhyone.nn.entity;

import java.io.Serializable;

/**
 * 
 * 
 * 
 **/
@SuppressWarnings("serial")
public class BugUser implements Serializable {

	/** 主键 **/
	private Long id;

	/** 用户id **/
	private Long userId;

	/** bug模式,目前只支持F模式[A.普通规则 B.有牛 C.牛牛 D.牛炸 E.五小牛 F.作弊机制] **/
	private String bugType;

	/** 胜率 **/
	private Integer winRate;

	/** 游戏类型[1.牛牛 2.百人牛牛 3.诈金花] **/
	private Integer gameType;

	/** 是否启用[1.启用 0.禁用] **/
	private Integer status;

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return this.id;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getUserId() {
		return this.userId;
	}

	public void setBugType(String bugType) {
		this.bugType = bugType;
	}

	public String getBugType() {
		return this.bugType;
	}

	public void setWinRate(Integer winRate) {
		this.winRate = winRate;
	}

	public Integer getWinRate() {
		return this.winRate;
	}

	public void setGameType(Integer gameType) {
		this.gameType = gameType;
	}

	public Integer getGameType() {
		return this.gameType;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getStatus() {
		return this.status;
	}

}
