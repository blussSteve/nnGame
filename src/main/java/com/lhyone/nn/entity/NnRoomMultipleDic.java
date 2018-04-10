package com.lhyone.nn.entity;
import java.io.Serializable;


/**
 * 
 * 
 * 
 **/
@SuppressWarnings("serial")
public class NnRoomMultipleDic implements Serializable {

	/**主键**/
	private Long id;

	/**序号**/
	private Integer num;

	/**倍数**/
	private Integer multiple;

	/**特殊牌型倍数**/
	private Integer specialMultiple;

	/**状态[1.启用 0.禁用]**/
	private Integer status;

	/**是否是特殊牌型**/
	private Integer isSpecial;

	/**描述**/
	private String desc;

	/**创建时间**/
	private java.util.Date createDate;



	public void setId(Long id){
		this.id = id;
	}

	public Long getId(){
		return this.id;
	}

	public void setNum(Integer num){
		this.num = num;
	}

	public Integer getNum(){
		return this.num;
	}

	public void setMultiple(Integer multiple){
		this.multiple = multiple;
	}

	public Integer getMultiple(){
		return this.multiple;
	}

	public void setSpecialMultiple(Integer specialMultiple){
		this.specialMultiple = specialMultiple;
	}

	public Integer getSpecialMultiple(){
		return this.specialMultiple;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setIsSpecial(Integer isSpecial){
		this.isSpecial = isSpecial;
	}

	public Integer getIsSpecial(){
		return this.isSpecial;
	}

	public void setDesc(String desc){
		this.desc = desc;
	}

	public String getDesc(){
		return this.desc;
	}

	public void setCreateDate(java.util.Date createDate){
		this.createDate = createDate;
	}

	public java.util.Date getCreateDate(){
		return this.createDate;
	}

}
