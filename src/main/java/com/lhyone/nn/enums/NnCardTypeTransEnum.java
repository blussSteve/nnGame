package com.lhyone.nn.enums;

public enum NnCardTypeTransEnum {
	
	SAN_PAI(0,0,"散牌"),
	NIU_DING(1,1,"牛丁"),
	NIU_2(2,2,"牛二"),
	NIU_3(3,3,"牛三"),
	NIU_4(4,4,"牛四"),
	NIU_5(5,5,"牛五"),
	NIU_6(6,6,"牛六"),
	NIU_7(7,7,"牛七"),
	NIU_8(8,8,"牛八"),
	NIU_9(9,9,"牛九"),
	NIU_NIU(10,10,"牛牛"),
	SHUN_ZI_NIU(11,11,"顺子牛"),
	TONG_HUA_NIU(12,12,"同花牛"),
	HU_LU_NIU(13,13,"葫芦牛"),
	FOUR_ZHA(14,14,"炸牛"),
	FIVE_BIG_NIU(15,15,"五花牛"),
	HUAN_LE_NIU(16,16,"欢乐牛"),
	FIVE_SMALL_NIU(17,17,"五小牛");
	
	private int orgType;
	private int destType;
	private String desc;
	
	
	private NnCardTypeTransEnum(int orgType, int destType, String desc) {
		this.orgType = orgType;
		this.destType = destType;
		this.desc = desc;
	}
	
	public static int getDestType(int orgType) {
        for (NnCardTypeTransEnum enums : NnCardTypeTransEnum.values()) {
            if (enums.orgType==orgType) {
                return enums.destType;
            }
        }
        return 0;
    }
	public int getOrgType() {
		return orgType;
	}
	public void setOrgType(int orgType) {
		this.orgType = orgType;
	}
	public int getDestType() {
		return destType;
	}
	public void setDestType(int destType) {
		this.destType = destType;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	
	
}
