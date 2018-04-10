package com.lhyone.nn.enums;


public enum NnCardTypeEnum {
	FANG_KUAI(1,"方块"),
	MEI_HUA(2,"梅花"),
	HONG_TAO(3,"红桃"),
	HEI_TAO(4,"黑桃");
	private int code;
	
	private String desc;

	public static NnCardTypeEnum getByCode(int code) {
        for (NnCardTypeEnum enums : NnCardTypeEnum.values()) {
            if (enums.code==code) {
                return enums;
            }
        }
        return null;
    }
	
	private NnCardTypeEnum(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
}
