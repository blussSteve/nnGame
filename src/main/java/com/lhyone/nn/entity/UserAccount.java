package com.lhyone.nn.entity;
import java.io.Serializable;


/**
 * 
 * 
 * 
 **/
@SuppressWarnings("serial")
public class UserAccount implements Serializable {

	/**主键**/
	private Long id;

	/**用户主键**/
	private Long userId;

	/**金币**/
	private Long curGold;

	/**总消耗金币**/
	private Long totalCostGold;

	/**账户类型[1.普通]**/
	private Integer accountType;

	/**账户到期日期**/
	private java.util.Date accountTypeEndDate;

	/**修改时间**/
	private java.util.Date updateDate;

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

	public void setCurGold(Long curGold){
		this.curGold = curGold;
	}

	public Long getCurGold(){
		return this.curGold;
	}

	public void setTotalCostGold(Long totalCostGold){
		this.totalCostGold = totalCostGold;
	}

	public Long getTotalCostGold(){
		return this.totalCostGold;
	}

	public void setAccountType(Integer accountType){
		this.accountType = accountType;
	}

	public Integer getAccountType(){
		return this.accountType;
	}

	public void setAccountTypeEndDate(java.util.Date accountTypeEndDate){
		this.accountTypeEndDate = accountTypeEndDate;
	}

	public java.util.Date getAccountTypeEndDate(){
		return this.accountTypeEndDate;
	}

	public void setUpdateDate(java.util.Date updateDate){
		this.updateDate = updateDate;
	}

	public java.util.Date getUpdateDate(){
		return this.updateDate;
	}

	public void setCreateDate(java.util.Date createDate){
		this.createDate = createDate;
	}

	public java.util.Date getCreateDate(){
		return this.createDate;
	}

}
