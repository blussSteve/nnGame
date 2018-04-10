package com.lhyone.nn.vo;

import java.util.List;

public class PushDoubleVo {

	/**上次被选中的推注用户*/
	private List<String> preUserId;
	
	/**被选中的用户*/
	private  List<String> beChoiceUserId;

	public List<String> getPreUserId() {
		return preUserId;
	}

	public void setPreUserId(List<String> preUserId) {
		this.preUserId = preUserId;
	}

	public List<String> getBeChoiceUserId() {
		return beChoiceUserId;
	}

	public void setBeChoiceUserId(List<String> beChoiceUserId) {
		this.beChoiceUserId = beChoiceUserId;
	}

	
}
