package com.lhyone.nn.enums;

public enum NnYesNoEnum {
	
	YES(1,"是"),
	NO(0,"否");
	
	private int code;
	private String desc;
	
	public static boolean isNull(int code){
		for(NnYesNoEnum enums:NnYesNoEnum.values()){
			
			if(enums.code==code){
				return true;
			}
		}
		return false;
	}
	
	
	private NnYesNoEnum(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}
	public int getCode() {
		return code;
	}
	public String getDesc() {
		return desc;
	}
	
	
}
