package com.lhyone.nn.enums;

/**
 * 道具字典
 * @author admin
 *
 */
public enum PropEnums {

	ONE(1,"鸡蛋",1),
	TOW(2,"番茄",2),
	THREE(3,"手雷",2),
	FOUR(4,"手枪",5);	
	private int num;
	private String name;
	private int costGold;
	
	private PropEnums(int num, String name, int costCard) {
		this.num = num;
		this.name = name;
		this.costGold = costCard;
	}
	public static PropEnums getByCode(int num) {
        for (PropEnums enums : PropEnums.values()) {
            if (enums.num==num) {
                return enums;
            }
        }
        return null;
    }

	public int getNum() {
		return num;
	}

	public String getName() {
		return name;
	}
	public int getCostGold() {
		return costGold;
	}

	
	
}
