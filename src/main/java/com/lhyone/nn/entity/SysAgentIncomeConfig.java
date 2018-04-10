package com.lhyone.nn.entity;
import java.io.Serializable;


/**
 * 
 * 
 * 
 **/
@SuppressWarnings("serial")
public class SysAgentIncomeConfig implements Serializable {

	/**主键**/
	private Long id;

	/**代理级别**/
	private Integer agentLevel;

	/**代理利率**/
	private Integer agentRate;

	/**游戏类型**/
	private Integer gameType;

	/**创建时间**/
	private java.util.Date createDate;



	public void setId(Long id){
		this.id = id;
	}

	public Long getId(){
		return this.id;
	}

	public void setAgentLevel(Integer agentLevel){
		this.agentLevel = agentLevel;
	}

	public Integer getAgentLevel(){
		return this.agentLevel;
	}

	public void setAgentRate(Integer agentRate){
		this.agentRate = agentRate;
	}

	public Integer getAgentRate(){
		return this.agentRate;
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
