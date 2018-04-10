package com.lhyone.nn.logic.handler;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.lhyone.nn.entity.NnRoom;
import com.lhyone.nn.pb.NnBean;
import com.lhyone.nn.util.NnConstans;
import com.lhyone.util.RedisUtil;

import io.netty.channel.ChannelHandlerContext;

public class NnWork implements Runnable {
	
	
	private NnBean.ReqMsg reqMsg;
	private int type;
	private ChannelHandlerContext ctx;
	public NnWork(NnBean.ReqMsg reqMsg,int type,ChannelHandlerContext ctx){
		this.reqMsg=reqMsg;
		this.type=type;
		this.ctx=ctx;
	}
	
	public void run() {
			fuGuiNn();
	}
	
	/**
	 * 富贵牛牛
	 */
	private void fuGuiNn(){
		switch (type) {
		case 1:
			NnManager.showMatchResult(reqMsg);
			break;
		case 2:
			NnManager.redaySendCard(reqMsg);
			break;
		case 3:
			NnManager.setLandlord(reqMsg);
			break;
		default:
			break;
		}
		
	}

}
