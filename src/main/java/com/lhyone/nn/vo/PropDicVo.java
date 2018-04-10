package com.lhyone.nn.vo;

import java.io.Serializable;

public class PropDicVo implements Serializable{
	
	private static final long serialVersionUID = 4892990714125574073L;
	private int num;
	private String name;
	private int costGold;
	
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getCostGold() {
		return costGold;
	}
	public void setCostGold(int costGold) {
		this.costGold = costGold;
	}
	
	
}
