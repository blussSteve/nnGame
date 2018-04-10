package com.lhyone.nn.enums;

public enum NnCardRuleEnum {
	
	NONE_NIU(1,"无牛"),
	X_NIU(2,"有牛"),
	NIU_NIU(3,"全牛"),
	SHUN_ZI_NIU(4,"顺子牛"),
	TONG_HUA_NIU(5,"同花牛"),
	HU_LU_NIU(6,"葫芦牛"),
	FOUR_ZHA(7,"炸牛"),
	FIVE_BIG_NIU(8,"五花牛"),
	HUAN_LE_NIU(9,"欢乐牛"),
	FIVE_SMALL_NIU(10,"五小牛");
	
	private int code;
	
	private String desc;

	private NnCardRuleEnum(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}
	public static NnCardRuleEnum getByCode(int code) {
        for (NnCardRuleEnum enums : NnCardRuleEnum.values()) {
            if (enums.code==code) {
                return enums;
            }
        }
        return null;
    }
	public int getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
	
	
	
	
}
