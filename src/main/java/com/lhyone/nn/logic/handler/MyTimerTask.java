package com.lhyone.nn.logic.handler;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.googlecode.protobuf.format.JsonFormat;
import com.lhyone.nn.enums.NnRoomMatchStatusEnum;
import com.lhyone.nn.enums.NnRspCodeEnum;
import com.lhyone.nn.enums.NnRspMsgTypeEnum;
import com.lhyone.nn.enums.NnTimeTaskEnum;
import com.lhyone.nn.enums.NnUserRoleEnum;
import com.lhyone.nn.enums.NnWrokEnum;
import com.lhyone.nn.enums.NnYesNoEnum;
import com.lhyone.nn.enums.NumEnum;
import com.lhyone.nn.pb.NnBean;
import com.lhyone.nn.pb.NnBean.ReqMsg;
import com.lhyone.nn.util.NnConstans;
import com.lhyone.nn.util.NnUtil;
import com.lhyone.nn.vo.GameTimoutVo;
import com.lhyone.util.RedisUtil;

public class MyTimerTask implements Runnable{
	 private static Logger logger = LogManager.getLogger(NnManager.class);
		
	private NnBean.ReqMsg reqMsg;
	private int type;
	private long startTime;
	public MyTimerTask(NnBean.ReqMsg reqMsg,int type,long startTime){
		this.reqMsg=reqMsg;
		this.type=type;
		this.startTime=startTime;
	}
	
	public void run() {
		nnTask();
	}
	 
	private void nnTask(){
		
	try{

		
		//标识位
		boolean flag=false;
		if(NnTimeTaskEnum.USER_REDAY.getCode()==type){
			
			 String time=RedisUtil.hget(NnConstans.NN_ROOM_USER_REDAY_TIME_PRE+reqMsg.getRoomNo(), reqMsg.getUserId()+"");
			
			 //判断当前定时是否超时，主要是为了防止定时器重复
			 if(time==null||startTime!=Long.parseLong(time)||!isRunTimer(Long.parseLong(time), type)){
			   logger.info("准备倒计时失效...");
			   return;
		    }
				
			NnBean.UserInfo.Builder userInfo=NnManager.getCurUser(reqMsg.getUserId(),reqMsg.getRoomNo());
			
			if(userInfo.getIsreday()==NnYesNoEnum.YES.getCode()){//如果已准备
				return ;
				
			}
			//剔除用户
			JSONObject jb=new JSONObject();
			jb.put("roomNo", reqMsg.getRoomNo());
			jb.put("userId", reqMsg.getUserId());
			NnManager.takeOutRoomUser(jb.toJSONString());
			
		}
		//如果是抢庄的定时器
		if(NnTimeTaskEnum.GRAB_LANDLORD.getCode()==type){
			
			  //判断当前定时是否超时，主要是为了防止定时器重复
			   GameTimoutVo gameTimoutVo=getGameTimoutVo(reqMsg);
			   if(gameTimoutVo==null||startTime!=gameTimoutVo.getLandlordTime()||!isRunTimer(gameTimoutVo.getLandlordTime(), type)){
				   logger.info("抢庄倒计时失效...");
				   return;
			   }
				  
				//判断此时状态是否可以抢庄
				
				String curStaus=RedisUtil.hget(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo());
				
				if(Integer.parseInt(curStaus)!=NnRoomMatchStatusEnum.GRAB_LANDLORD_STATUS.getCode()){
					return ;
				}
				logger.info("执行抢庄倒计时......");
				
				//将没抢庄的设置参与抢庄操作
				Set<String> userSet=NnManager.getAllMatchUserSet(reqMsg.getRoomNo());
				
				for(String key:userSet){
					
					boolean isGrabLandlord= RedisUtil.hexists(NnConstans.NN_ROOM_LANDLORD_USER_PRE+reqMsg.getRoomNo(), key);//设置该用户是已抢庄
					
					if(!isGrabLandlord){
							NnBean.UserInfo.Builder user=NnManager.getCurUser(Long.parseLong(key),reqMsg.getRoomNo());
						
							if(user.getIsGrabLandlord()==0){
								flag=true;
								NnBean.RspMsg.Builder rspMsg=NnBean.RspMsg.newBuilder();
								user.setIsGrabLandlord(NnYesNoEnum.YES.getCode());
								user.setScoreType(NnUtil.getLandlordMenuMinScore());
								
								RedisUtil.hset(NnConstans.NN_ROOM_USER_INFO_PRE+reqMsg.getRoomNo(), key,JsonFormat.printToString(user.build()));
								
								//3.增加用户抢庄操作【是否参与抢庄操作，抢庄倍数，抢庄时间】
								RedisUtil.hset(NnConstans.NN_ROOM_LANDLORD_USER_PRE+reqMsg.getRoomNo(),key,"0,0,"+System.currentTimeMillis()+"");//设置该用户是已抢庄
								
								
								
								rspMsg.setOperateType(NnRspMsgTypeEnum.GRAB_LANDLORD_FEEDBACK.getCode());
								rspMsg.setCode(NnRspCodeEnum.$0000.getCode());
								rspMsg.setMsg(NnRspCodeEnum.$0000.getMsg());
								
								reqMsg=ReqMsg.newBuilder(reqMsg).setUserId(Long.parseLong(key)).build();
								
								NnManager.batchSendLandlord(reqMsg);
								
							}
						
					}
					
				}
				
				if(flag){
					//抢庄
					NnManager.setLandlord(reqMsg);
				}
				
			
		}
		
		if(NnTimeTaskEnum.FARMER_DOUBLEX.getCode()==type){
			
			//判断当前定时是否超时，主要是为了防止定时器重复
			GameTimoutVo gameTimoutVo=getGameTimoutVo(reqMsg);
			if(gameTimoutVo==null||startTime!=gameTimoutVo.getFarmerTime()||!isRunTimer(gameTimoutVo.getFarmerTime(), type)){
				logger.info("闲家加倍倒计时失效....");
				return;
			}
			    
			//判断此时状态是否可以设置闲家加倍
			
			String curStaus=RedisUtil.hget(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo());
			
			if(Integer.parseInt(curStaus)!=NnRoomMatchStatusEnum.FARMER_STATUS.getCode()){
				return;
				
			}
			logger.info("执行明闲家加倍倒计时......");
			//将没抢庄的设置参与抢庄操作
			Set<String> userSet=NnManager.getAllMatchUserSet(reqMsg.getRoomNo());
			
			for(String key:userSet){
				
				NnBean.UserInfo.Builder user=NnManager.getCurUser(Long.parseLong(key),reqMsg.getRoomNo());
				
				if(user.getPlayerType()==NnUserRoleEnum.FARMER.getCode()){
					
					if(user.getIsGrabFarmer()==0){
						flag=true;
						 user.setScoreType(NnUtil.getFarmerMenuMinScore());
						 user.setIsGrabFarmer(NnYesNoEnum.YES.getCode());
						
						 RedisUtil.hset(NnConstans.NN_ROOM_USER_INFO_PRE+reqMsg.getRoomNo(), key,JsonFormat.printToString(user.build()));
						 RedisUtil.hset(NnConstans.NN_ROOM_FARMER_USER_PRE+reqMsg.getRoomNo(),key,NnUtil.getFarmerMenuMinScore()+"");
						 
						 reqMsg=ReqMsg.newBuilder(reqMsg).setUserId(Long.parseLong(key)).build();
						 
						 NnManager.batchSendSetFarmer(reqMsg);
						 
					}
					
				}
			}
			
			if(flag){
				//发送最后一张牌
				 NnManager.sendLastCard(reqMsg);
			}
			
			
		}
		//名牌倒计时
		if(NnTimeTaskEnum.SHOW_CARD_RESULT.getCode()==type){
			
			//判断当前定时是否超时，主要是为了防止定时器重复
			GameTimoutVo gameTimoutVo=getGameTimoutVo(reqMsg);
			if(gameTimoutVo==null||startTime!=gameTimoutVo.getShowMatchResultTime()||!isRunTimer(gameTimoutVo.getShowMatchResultTime(), type)){
				logger.info("名牌倒计时失效....");
				return;
				
			}
			
			//只有当前状态在名牌阶段才能调用此接口
			String curStaus=RedisUtil.hget(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo());
			
			if(Integer.parseInt(curStaus)!=NnRoomMatchStatusEnum.PLAY_GAME_STATUS.getCode()){
				return;
				
			}
			logger.info("执行明牌倒计时......");
			//将没抢庄的设置参与抢庄操作
			Set<String> userSet=NnManager.getAllMatchUserSet(reqMsg.getRoomNo());
			
			for(String key:userSet){
				NnBean.UserInfo.Builder user=NnManager.getCurUser(Long.parseLong(key),reqMsg.getRoomNo());
				
					NnBean.RspMsg.Builder rspMsg=NnBean.RspMsg.newBuilder();
					if(user.getIsShowCard()==NnYesNoEnum.YES.getCode()){
						continue;
					}
					flag=true;
					user.setIsShowCard(NnYesNoEnum.YES.getCode());
					RedisUtil.hset(NnConstans.NN_ROOM_USER_INFO_PRE+reqMsg.getRoomNo(),key,JsonFormat.printToString(user.build()));
			
					rspMsg.setOperateType(NnRspMsgTypeEnum.SHOW_CARD_FEEDBACK.getCode());
					rspMsg.setCode(NnRspCodeEnum.$0000.getCode());
					rspMsg.setMsg(NnRspCodeEnum.$0000.getMsg());
					
					String channelId=RedisUtil.hget(NnConstans.NN_USER_CHANNEL_PRE,key);
					
					NnManager.sendMsg(rspMsg.build(), channelId);
					
					reqMsg=ReqMsg.newBuilder(reqMsg).setUserId(Long.parseLong(key)).build();
					
					NnManager.batchSendShowCard(reqMsg);
				
			
			}
			
			if(flag){
				NnManager.showMatchResult(reqMsg);
				
			}
			
		}
		}catch(Exception e){
			logger.info(e.getMessage(),e);
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 获取游戏超时时间
	 * @param reqMsg
	 * @return
	 */
	private static GameTimoutVo getGameTimoutVo(ReqMsg reqMsg){
		
		String str=RedisUtil.hget(NnConstans.NN_REST_TIME_PRE, reqMsg.getRoomNo());
		
		if(str!=null){
			return JSONObject.parseObject(str, GameTimoutVo.class);
		}
		return null;
	}
	
	
	private static boolean isRunTimer(long time,int type){
		
		long curTime=System.currentTimeMillis();
		long diffTime=Math.abs((curTime-time)/1000);
		long timer=0;
		
		if(NnTimeTaskEnum.USER_REDAY.getCode()==type){
			timer=NnConstans.USER_RDAY_TIME;
		}else if(NnTimeTaskEnum.GRAB_LANDLORD.getCode()==type){
			timer=NnConstans.GRAB_LANDLORD_TIME;
		}else if(NnTimeTaskEnum.FARMER_DOUBLEX.getCode()==type){
			timer=NnConstans.FARMER_TIME;
		}else if(NnTimeTaskEnum.SHOW_CARD_RESULT.getCode()==type){
			timer=NnConstans.SHOW_MATCH_TIME;
		}
		
		//延迟两秒
		if(diffTime<(timer+2)){
			return true;
		}
		return false;
	}

}
