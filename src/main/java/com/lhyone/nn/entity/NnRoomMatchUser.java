package com.lhyone.nn.entity;
import java.io.Serializable;


/**
 * 
 * 
 * 
 **/
@SuppressWarnings("serial")
public class NnRoomMatchUser implements Serializable {

	/**主键**/
	private Long id;

	/**用户id**/
	private Long userId;

	/**房间id**/
	private Long roomId;

	/**房间号**/
	private String roomNo;

	/**比赛序号**/
	private Integer matchNum;

	/**消耗前总金币数**/
	private Long totalGold;

	/**底池**/
	private Integer baseGold;

	/**赚取金币**/
	private Integer winGold;

	/**消耗金币**/
	private Integer costGold;

	/**玩家类型[1.庄家 2.闲家]**/
	private Integer playerRole;

	/**卡**/
	private String cards;

	/**卡类型**/
	private Integer cardType;

	/**是否胜利[1.是 0.否]**/
	private Integer isWin;

	/**倍数**/
	private Integer doublex;

	/**流水号主要是做标识用**/
	private String orderNo;

	/**是否开启bug[]**/
	private Integer isBug;

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

	public void setMatchNum(Integer matchNum){
		this.matchNum = matchNum;
	}

	public Integer getMatchNum(){
		return this.matchNum;
	}

	public void setTotalGold(Long totalGold){
		this.totalGold = totalGold;
	}

	public Long getTotalGold(){
		return this.totalGold;
	}

	public void setBaseGold(Integer baseGold){
		this.baseGold = baseGold;
	}

	public Integer getBaseGold(){
		return this.baseGold;
	}

	public void setWinGold(Integer winGold){
		this.winGold = winGold;
	}

	public Integer getWinGold(){
		return this.winGold;
	}

	public void setCostGold(Integer costGold){
		this.costGold = costGold;
	}

	public Integer getCostGold(){
		return this.costGold;
	}

	public void setPlayerRole(Integer playerRole){
		this.playerRole = playerRole;
	}

	public Integer getPlayerRole(){
		return this.playerRole;
	}

	public void setCards(String cards){
		this.cards = cards;
	}

	public String getCards(){
		return this.cards;
	}

	public void setCardType(Integer cardType){
		this.cardType = cardType;
	}

	public Integer getCardType(){
		return this.cardType;
	}

	public void setIsWin(Integer isWin){
		this.isWin = isWin;
	}

	public Integer getIsWin(){
		return this.isWin;
	}

	public void setDoublex(Integer doublex){
		this.doublex = doublex;
	}

	public Integer getDoublex(){
		return this.doublex;
	}

	public void setOrderNo(String orderNo){
		this.orderNo = orderNo;
	}

	public String getOrderNo(){
		return this.orderNo;
	}

	public void setIsBug(Integer isBug){
		this.isBug = isBug;
	}

	public Integer getIsBug(){
		return this.isBug;
	}

	public void setCreateDate(java.util.Date createDate){
		this.createDate = createDate;
	}

	public java.util.Date getCreateDate(){
		return this.createDate;
	}

}
