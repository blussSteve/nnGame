package com.lhyone.nn.enums;

public enum NnCardNumEnum {
	_1(1,"A"),
	_2(2,"2"),
	_3(3,"3"),
	_4(4,"4"),
	_5(5,"5"),
	_6(6,"6"),
	_7(7,"7"),
	_8(8,"8"),
	_9(9,"9"),
	_10(10,"10"),
	_11(11,"J"),
	_12(12,"Q"),
	_13(13,"K");
	
	private int code;
	
	private String desc;

	private NnCardNumEnum(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static NnCardNumEnum getByCode(int code) {
        for (NnCardNumEnum enums : NnCardNumEnum.values()) {
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
