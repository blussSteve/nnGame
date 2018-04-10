/* 
  * @(#) YesOrNo.java 2014-5-26
 * Copyright (c) 2014 lhy Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * hqb Technologies. You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the agreements
 * you entered into with hqb Technologies.
 *
 */
package com.lhyone.nn.enums;

/**
 * 是否枚举
 *
 * @version 1.0
 */
public enum NumEnum {
    ZERO(0l, 0, "0", "zero", "ZERO"),
    ONE(1l, 1, "1", "one", "ONE"),
    TWO(2l, 2, "2", "two", "TWO"),
    THREE(3l, 3, "3", "three", "THREE"),
    FOUR(4l, 4, "4", "four", "FOUR"),
    FIVE(5l, 5, "5", "five", "FIVE"),
    SIX(6l, 6, "6", "six", "SIX"),
    SEVEN(7l, 7, "7", "seven", "SEVEN"),
    EIGHT(8l, 8, "8", "eight", "EIGHT"),
    NINE(9l, 9, "9", "nine", "NINE"),
    TEN(10l, 10, "10", "one", "TEN"),
    ELEVEN(11l, 11, "11", "eleven", "ELEVEN"),
    TWELVE(12l, 12, "12", "twelve", "TWELVE"),
    THIRTEEN(13l, 13, "13", "thirteen", "THIRTEEN"),
    FOURTEEN(14l, 14, "14", "fourteen", "FOURTEEN"),
    FIFTEEN(15l, 15, "15", "fifteen", "FIFTEEN"),
    SIXTEEN(16l, 16, "16", "sixteen", "SIXTEEN"),
    SEVETEEN(17l, 17, "17", "seveteen", "SEVETEEN"),
    EIGHTEEN(18l, 18, "18", "eighteen", "EIGHTEEN"),
    NINETEEN(19l, 19, "19", "nineteen", "NINETEEN"),
    TWENTY(20l, 20, "20", "twenty", "TWENTY");

    private Long numLong;

    private Integer numInteger;

    private String numString;

    private String numEnglishLower;

    private String numEnglishUpper;


    private NumEnum(Long numLong, Integer numInteger, String numString, String numEnglishLower, String numEnglishUpper) {
        this.numLong = numLong;
        this.numInteger = numInteger;
        this.numString = numString;
        this.numEnglishLower = numEnglishLower;
        this.numEnglishUpper = numEnglishUpper;

    }

    public Long getNumLong() {
        return numLong;
    }

    public Integer getNumInteger() {
        return numInteger;
    }

    public String getNumString() {
        return numString;
    }

    public String getNumEnglishLower() {
        return numEnglishLower;
    }

    public String getNumEnglishUpper() {
        return numEnglishUpper;
    }
}
