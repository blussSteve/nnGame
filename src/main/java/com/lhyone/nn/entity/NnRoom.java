package com.lhyone.nn.entity;
import java.io.Serializable;


/** 
 * 
 * 
 * 
 **/
public class NnRoom implements Serializable {
	

	private static final long serialVersionUID = 6034766604025846972L;

	/**主键**/
	private Long id;

	/**房间类型[1.趣味牛牛 2.8人牛牛]**/
	private Integer roomType;

	/**创建人id**/
	private Long createUserId;

	/**拥有者id**/
	private Long ownUserId;

	/**服务id**/
	private Integer serverId;

	/**房间编号**/
	private String roomNo;

	/**房间最大人数**/
	private Integer roomPersonCount;

	/**比赛局数**/
	private Integer roomMatchCount;

	/**实际比赛局数**/
	private Integer roomRealMatchCount;

	/**最大倍数**/
	private Integer roomMaxDouble;

	/**房间状态[1.初始状态 2.已解散 3.已结束]**/
	private Integer roomStatus;

	/**房间翻倍规则**/
	private String roomDoubleRule;

	/**进房金币下限**/
	private Long inLimitGold;

	/**离开房间最低金币下限**/
	private Long outLimitGold;

	/**消耗金币**/
	private java.math.BigDecimal costGold;

	/**底分**/
	private Integer baseGold;

	/**是否展示**/
	private Integer isShow;

	/**创建时间**/
	private java.util.Date createDate;

	/**修改时间**/
	private java.util.Date updateDate;



	public void setId(Long id){
		this.id = id;
	}

	public Long getId(){
		return this.id;
	}

	public void setRoomType(Integer roomType){
		this.roomType = roomType;
	}

	public Integer getRoomType(){
		return this.roomType;
	}

	public void setCreateUserId(Long createUserId){
		this.createUserId = createUserId;
	}

	public Long getCreateUserId(){
		return this.createUserId;
	}

	public void setOwnUserId(Long ownUserId){
		this.ownUserId = ownUserId;
	}

	public Long getOwnUserId(){
		return this.ownUserId;
	}

	public void setServerId(Integer serverId){
		this.serverId = serverId;
	}

	public Integer getServerId(){
		return this.serverId;
	}

	public void setRoomNo(String roomNo){
		this.roomNo = roomNo;
	}

	public String getRoomNo(){
		return this.roomNo;
	}

	public void setRoomPersonCount(Integer roomPersonCount){
		this.roomPersonCount = roomPersonCount;
	}

	public Integer getRoomPersonCount(){
		return this.roomPersonCount;
	}

	public void setRoomMatchCount(Integer roomMatchCount){
		this.roomMatchCount = roomMatchCount;
	}

	public Integer getRoomMatchCount(){
		return this.roomMatchCount;
	}

	public void setRoomRealMatchCount(Integer roomRealMatchCount){
		this.roomRealMatchCount = roomRealMatchCount;
	}

	public Integer getRoomRealMatchCount(){
		return this.roomRealMatchCount;
	}

	public void setRoomMaxDouble(Integer roomMaxDouble){
		this.roomMaxDouble = roomMaxDouble;
	}

	public Integer getRoomMaxDouble(){
		return this.roomMaxDouble;
	}

	public void setRoomStatus(Integer roomStatus){
		this.roomStatus = roomStatus;
	}

	public Integer getRoomStatus(){
		return this.roomStatus;
	}

	public void setRoomDoubleRule(String roomDoubleRule){
		this.roomDoubleRule = roomDoubleRule;
	}

	public String getRoomDoubleRule(){
		return this.roomDoubleRule;
	}

	public void setInLimitGold(Long inLimitGold){
		this.inLimitGold = inLimitGold;
	}

	public Long getInLimitGold(){
		return this.inLimitGold;
	}

	public void setOutLimitGold(Long outLimitGold){
		this.outLimitGold = outLimitGold;
	}

	public Long getOutLimitGold(){
		return this.outLimitGold;
	}

	public void setCostGold(java.math.BigDecimal costGold){
		this.costGold = costGold;
	}

	public java.math.BigDecimal getCostGold(){
		return this.costGold;
	}

	public void setBaseGold(Integer baseGold){
		this.baseGold = baseGold;
	}

	public Integer getBaseGold(){
		return this.baseGold;
	}

	public void setIsShow(Integer isShow){
		this.isShow = isShow;
	}

	public Integer getIsShow(){
		return this.isShow;
	}

	public void setCreateDate(java.util.Date createDate){
		this.createDate = createDate;
	}

	public java.util.Date getCreateDate(){
		return this.createDate;
	}

	public void setUpdateDate(java.util.Date updateDate){
		this.updateDate = updateDate;
	}

	public java.util.Date getUpdateDate(){
		return this.updateDate;
	}

}
