package com.lhyone.nn.entity;
import java.io.Serializable;


/**
 * 
 * 
 * 
 **/
@SuppressWarnings("serial")
public class GoldRecord implements Serializable {

	/**主键**/
	private Long id;

	/**user_id**/
	private Long userId;

	/**流水号**/
	private String orderNo;

	/**金币数**/
	private Integer goldCount;

	/**类型[1.充值 2.牛牛服务费 3.百人牛牛服务费 4.诈金花服务费]**/
	private Integer costType;

	/**绑定的业务流水id**/
	private Long bindId;

	/**创建时间**/
	private java.sql.Timestamp createDate;



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

	public void setOrderNo(String orderNo){
		this.orderNo = orderNo;
	}

	public String getOrderNo(){
		return this.orderNo;
	}

	public void setGoldCount(Integer goldCount){
		this.goldCount = goldCount;
	}

	public Integer getGoldCount(){
		return this.goldCount;
	}

	public void setCostType(Integer costType){
		this.costType = costType;
	}

	public Integer getCostType(){
		return this.costType;
	}

	public void setBindId(Long bindId){
		this.bindId = bindId;
	}

	public Long getBindId(){
		return this.bindId;
	}

	public void setCreateDate(java.sql.Timestamp createDate){
		this.createDate = createDate;
	}

	public java.sql.Timestamp getCreateDate(){
		return this.createDate;
	}

}
