package com.lhyone.nn.entity;
import java.io.Serializable;


/**
 * 
 * 
 * 
 **/
@SuppressWarnings("serial")
public class UserMatchRecord implements Serializable {

	/**id**/
	private Long id;

	/**用户id**/
	private Long userId;

	/**赚取金币**/
	private Integer winGold;

	/**游戏类型[1.牛牛 2.百人牛牛 3.诈金花]**/
	private Integer gameType;

	/**房间id**/
	private Long roomId;

	/**绑定的相关业务id**/
	private Long bindId;

	/**备注**/
	private String mark;

	/**流水号**/
	private String orderNo;

	/**创建时间**/
	private java.util.Date createDate;



	public void setId(Long id){
		this.id = id;
	}

	public Long getId(){
		return this.id;
	}

	public void setUserId(Long userId){
		this.userId = userId;
	}

	public Long getUserId(){
		return this.userId;
	}

	public void setWinGold(Integer winGold){
		this.winGold = winGold;
	}

	public Integer getWinGold(){
		return this.winGold;
	}

	public void setGameType(Integer gameType){
		this.gameType = gameType;
	}

	public Integer getGameType(){
		return this.gameType;
	}

	public void setRoomId(Long roomId){
		this.roomId = roomId;
	}

	public Long getRoomId(){
		return this.roomId;
	}

	public void setBindId(Long bindId){
		this.bindId = bindId;
	}

	public Long getBindId(){
		return this.bindId;
	}

	public void setMark(String mark){
		this.mark = mark;
	}

	public String getMark(){
		return this.mark;
	}

	public void setOrderNo(String orderNo){
		this.orderNo = orderNo;
	}

	public String getOrderNo(){
		return this.orderNo;
	}

	public void setCreateDate(java.util.Date createDate){
		this.createDate = createDate;
	}

	public java.util.Date getCreateDate(){
		return this.createDate;
	}

}
