package com.lhyone.nn.entity;
import java.io.Serializable;


/**
 * 
 * 
 * 
 **/
@SuppressWarnings("serial")
public class JoinRoomUser implements Serializable {

	/**房间id**/
	private Long roomId;

	/**房间号**/
	private String roomNo;

	/**用户id**/
	private Long userId;

	/**状态[1.游客 2.玩家]**/
	private Integer status;

	/**游戏类型[1.牛牛 2.百人牛牛 3.诈金花]**/
	private Integer gameType;

	/**创建时间**/
	private java.util.Date createDate;



	public void setRoomId(Long roomId){
		this.roomId = roomId;
	}

	public Long getRoomId(){
		return this.roomId;
	}

	public void setRoomNo(String roomNo){
		this.roomNo = roomNo;
	}

	public String getRoomNo(){
		return this.roomNo;
	}

	public void setUserId(Long userId){
		this.userId = userId;
	}

	public Long getUserId(){
		return this.userId;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setGameType(Integer gameType){
		this.gameType = gameType;
	}

	public Integer getGameType(){
		return this.gameType;
	}

	public void setCreateDate(java.util.Date createDate){
		this.createDate = createDate;
	}

	public java.util.Date getCreateDate(){
		return this.createDate;
	}

}
