package com.lhyone.nn.logic.handler;

import java.util.Set;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.googlecode.protobuf.format.JsonFormat;
import com.lhyone.nn.enums.NnRoomMatchStatusEnum;
import com.lhyone.nn.enums.NnRspCodeEnum;
import com.lhyone.nn.enums.NnRspMsgTypeEnum;
import com.lhyone.nn.enums.NnTimeTaskEnum;
import com.lhyone.nn.enums.NnUserRoleEnum;
import com.lhyone.nn.enums.NnYesNoEnum;
import com.lhyone.nn.pb.NnBean;
import com.lhyone.nn.pb.NnBean.ReqMsg;
import com.lhyone.nn.util.LocalCacheUtil;
import com.lhyone.nn.util.NnConstans;
import com.lhyone.nn.util.NnUtil;
import com.lhyone.nn.vo.GameTimoutVo;
import com.lhyone.util.RedisUtil;

public class MyTimerTask implements Runnable{
	 private static Logger logger = LogManager.getLogger(NnManager.class);
		
	private NnBean.ReqMsg reqMsg;
	private int type;
	private long timestamp;
	public MyTimerTask(NnBean.ReqMsg reqMsg,int type,long timestamp){
		this.reqMsg=reqMsg;
		this.type=type;
		this.timestamp=timestamp;
	}
	
	public void run() {
		nnTask();
	}
	 
	private void nnTask(){
		
	try{

		
		//标识位
		boolean flag=false;
		
		if(NnTimeTaskEnum.LISTEN_TIME.getCode()==type){
			RedisUtil.set(NnConstans.NN_SYS_CUR_TIME_CACHE, System.currentTimeMillis()+"");
		}else{
			GameTimoutVo time=getGameTimoutVo(reqMsg);
			if(NnTimeTaskEnum.USER_REDAY.getCode()==type){
				GameTimoutVo timeVo= getUserTimeVo(reqMsg.getRoomNo(), reqMsg.getUserId()+"");
			 //判断当前定时是否超时，主要是为了防止定时器重复
			 if(timeVo==null||timeVo.getRestTimeType()!=type||timeVo.getRestTime()!=timestamp){
			   logger.info("准备倒计时失效...");
			   return;
		    }
				
			NnBean.UserInfo.Builder userInfo=NnManager.getCurUser(reqMsg.getUserId(),reqMsg.getRoomNo());
			
			if(userInfo.getIsreday()==NnYesNoEnum.YES.getCode()){//如果已准备
				return ;
				
			}
			if(LocalCacheUtil.hexist(NnConstans.NN_CLOSE_ROOM_LOCK_REQ, reqMsg.getRoomNo()+"")){
				synchronized (this) {
					//剔除用户
					JSONObject jb=new JSONObject();
					jb.put("roomNo", reqMsg.getRoomNo());
					jb.put("userId", reqMsg.getUserId());
					NnManager.takeOutRoomUser(jb.toJSONString());
				}
			}else{
				LocalCacheUtil.hset(NnConstans.NN_CLOSE_ROOM_LOCK_REQ, reqMsg.getRoomNo()+"", System.currentTimeMillis());
				//剔除用户
				JSONObject jb=new JSONObject();
				jb.put("roomNo", reqMsg.getRoomNo());
				jb.put("userId", reqMsg.getUserId());
				NnManager.takeOutRoomUser(jb.toJSONString());
			}
			
			
			
		}else if(NnTimeTaskEnum.USER_MATCH_END_REDAY.getCode()==type){
			GameTimoutVo timeVo= getUserTimeVo(reqMsg.getRoomNo(), reqMsg.getUserId()+"");
		 //判断当前定时是否超时，主要是为了防止定时器重复
		 if(timeVo==null||timeVo.getRestTimeType()!=type||timeVo.getRestTime()!=timestamp){
		   logger.info("准备倒计时失效...");
		   return;
	    }
			
		NnBean.UserInfo.Builder userInfo=NnManager.getCurUser(reqMsg.getUserId(),reqMsg.getRoomNo());
		
		if(userInfo.getIsreday()==NnYesNoEnum.YES.getCode()){//如果已准备
			return ;
			
		}
		if(LocalCacheUtil.hexist(NnConstans.NN_CLOSE_ROOM_LOCK_REQ, reqMsg.getRoomNo()+"")){
			synchronized (this) {
				Thread.sleep(200, RandomUtils.nextInt(200, 500));
				//剔除用户
				JSONObject jb=new JSONObject();
				jb.put("roomNo", reqMsg.getRoomNo());
				jb.put("userId", reqMsg.getUserId());
				NnManager.takeOutRoomUser(jb.toJSONString());
			}
		}else{
			LocalCacheUtil.hset(NnConstans.NN_CLOSE_ROOM_LOCK_REQ, reqMsg.getRoomNo()+"", System.currentTimeMillis());
			//剔除用户
			JSONObject jb=new JSONObject();
			jb.put("roomNo", reqMsg.getRoomNo());
			jb.put("userId", reqMsg.getUserId());
			NnManager.takeOutRoomUser(jb.toJSONString());
		}
		
		
		
	}else if(NnTimeTaskEnum.GRAB_LANDLORD.getCode()==type&&time.getRestTimeType()==type){//如果是抢庄的定时器
			   GameTimoutVo timeVo= getGameTimoutVo(reqMsg);
			   if(time.getRestTimeType()!=type||timeVo.getRestTime()!=timestamp){
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
				
			
		}else if(NnTimeTaskEnum.FARMER_DOUBLEX.getCode()==type&&time.getRestTimeType()==type){
			GameTimoutVo timeVo= getGameTimoutVo(reqMsg);
			//判断当前定时是否超时，主要是为了防止定时器重复
			if(time.getRestTimeType()!=type||timeVo.getRestTime()!=timestamp){
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
			
			
		}else if(NnTimeTaskEnum.SHOW_CARD_RESULT.getCode()==type&&time.getRestTimeType()==type){//名牌倒计时
			
			GameTimoutVo timeVo= getGameTimoutVo(reqMsg);
			
			if(timeVo.getRestTime()!=timestamp){
				logger.info("执行名牌倒计时失效......");
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
			
		}else if(NnTimeTaskEnum.SHOW_CARD_RESULT_REDAY.getCode()==type&&time.getRestTimeType()==type){//名牌准备倒计时
			
			GameTimoutVo timeVo= getGameTimoutVo(reqMsg);
			
			if(timeVo.getRestTime()!=timestamp){
				logger.info("执行名牌倒准备计时失效......");
				return;
			}
			
			Set<String> allUser=NnManager.getAllMatchUserSet(reqMsg.getRoomNo());
			String sysTimeStr=RedisUtil.get(NnConstans.NN_SYS_CUR_TIME_CACHE);
			long sysTime=0;
			if(!StringUtils.isEmpty(sysTimeStr)){
				sysTime=Long.parseLong(sysTimeStr);
			}
			
			//剔除用户
			JSONObject jb=new JSONObject();
			jb.put("roomNo", reqMsg.getRoomNo());
			
			for(String key:allUser){
				
				String timeStr=RedisUtil.hget(NnConstans.NN_ROOM_USER_REDAY_TIME_PRE+reqMsg.getRoomNo(),key);
				if(StringUtils.isEmpty(timeStr)){
					continue;
				}
				
				int redayTime = (int) (NnConstans.USER_RDAY_TIME - (sysTime - Long.parseLong(timeStr)) / 1000);
				System.out.println(redayTime);
			
				//剔除用户
				if(1>=redayTime){
					NnBean.UserInfo.Builder curUser=NnManager.getCurUser(Long.parseLong(key), reqMsg.getRoomNo());
					if(curUser.getIsreday()==NnYesNoEnum.NO.getCode()){
						jb.put("userId",key);
						NnManager.takeOutRoomUser(jb.toJSONString());
					}
				}
				
			}
		}
		}
		}catch(Exception e){
			System.out.println(e.getMessage());
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
		if(null==reqMsg){
			return null;
		}
		String str=RedisUtil.hget(NnConstans.NN_REST_TIME_PRE, reqMsg.getRoomNo());
		
		if(str!=null){
			return JSONObject.parseObject(str, GameTimoutVo.class);
		}
		return null;
	}

	
	private static GameTimoutVo getUserTimeVo(String roomNo,String userId){
		String str = RedisUtil.hget(NnConstans.NN_REST_TIME_PRE + roomNo, userId);
		if(StringUtils.isEmpty(str)){
			return null;
		}
		return JSONObject.parseObject(str, GameTimoutVo.class);
	}
	

}
