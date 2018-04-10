package com.lhyone.nn.enums;

public enum NnRspCodeEnum {
	
	/**成功*/
	$0000("0000","成功"),
	
	/**用户未授权*/
	$0001("0001","用户未授权"),
	
	/**加倍规则设置错误*/
	$0002("0002","加倍规则设置错误"),
	
	/**操作类型不合法*/
	$0003("0003","操作类型不合法"),
	
	/**无效参数*/
	$0004("0004","无效参数"),
	
	/**请勿重复提交*/
	$0005("0005","请勿重复提交"),
	
	/**系统繁忙请稍后重试*/
	$0006("0006","系统繁忙请稍后重试"),
	
	/**************************************房间信息1001-1110编码****************************************************/
	/**房间不存在*/
	$1001("1001","房间不存在"),
	
	/**上局比赛未结束，无法加入此房间*/
	$1002("1002","上局比赛未结束，无法加入此房间"),
	
	/**用户已加入其它房间*/
	$1004("1004","用户已加入其它房间"),
	
	/**房间人数已满或者已开赛*/
	$1005("1005","房间人数已满或者已开赛"),
	
	/**比赛为开赛，非房主无法解散房间*/
	$1008("1008","比赛为开赛，非房主无法解散房间"),
	
	/***************************************用户操作错误1101-1201******************************************************************************/
	/**用户退出房间*/
	$1101("1101","用户退出房间"),
	/**非此房间用户无法操作*/
	$1102("1102","非此房间用户无法操作"),
	
	/**您已抢庄，请勿重复操作*/
	$1107("1107","您已抢庄，请勿重复操作"),
	
	/**您已明牌，请勿重复操作*/
	$1110("1110","您已明牌，请勿重复操作"),
	
	/**您已申请解散房间，请勿重复操作*/
	$1111("1111","您已申请解散房间，请勿重复操作"),
	
	/**同意或者拒绝解散房间,请勿重复操作*/
	$1112("1112","请勿重复操作"),
	
	/**您已准备，请勿重复操作*/
	$1113("1113","您已准备，请勿重复操作"),
	
	/**闲家设置底分，请勿重复操作*/
	$1114("1114","您已设置底分请勿重复操作"),
	
	/**已点击开始下一场比赛按钮，请勿重复操作*/
	$1115("1115","请勿重复操作"),
	
	/**房卡不足*/
	$1116("1116","房卡不足"),
	
	/**房主无法退出房间*/
	$1117("1117","房主无法退出房间"),
	
	/**比赛已开赛，无法退出房间*/
	$1118("1118","比赛已开赛，无法退出房间"),
	
	/**您的金币不足以参加比赛*/
	$1119("1119","您的金币不足以参加比赛"),
	
	/**道具卡不足，无法完成本次支付*/
	$1120("1120","道具卡不足，无法完成本次支付"),
	
	/**您已同意解散房间，请等待他人同意*/
	$1121("1121","您已同意解散房间，请等待他人同意"),
	
	/***************************************每100类型递增*******************************************************************************/
	/**未知异常*/
	$9999("9999","未知异常");
	
	private String code;
	
	private String msg;
	

	private NnRspCodeEnum(String code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	public static NnRspCodeEnum getByCode(String code) {
        for (NnRspCodeEnum enums : NnRspCodeEnum.values()) {
            if (enums.code.equals(code)) {
                return enums;
            }
        }
        return null;
    }
	public String getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}
	
	
}
