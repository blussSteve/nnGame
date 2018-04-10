package com.lhyone.nn.logic.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lhyone.nn.entity.NnRoom;
import com.lhyone.nn.enums.NnPushMsgTypeEnum;
import com.lhyone.nn.enums.NnReqMsgTypeEnum;
import com.lhyone.nn.enums.NnRspCodeEnum;
import com.lhyone.nn.pb.NnBean;
import com.lhyone.nn.util.CommUtil;
import com.lhyone.nn.util.NnConstans;
import com.lhyone.util.RedisUtil;

import io.netty.channel.ChannelHandlerContext;

/**
 * 
 * Created by Think on 2017/8/15.
 */
public class NnTask implements Runnable {
	private static Logger logger=LogManager.getLogger(NnTask.class);
    private ChannelHandlerContext ctx;
    private NnBean.ReqMsg reqMsg;
    public NnTask(ChannelHandlerContext ctx, NnBean.ReqMsg reqMsg){
        this.ctx=ctx;
        this.reqMsg=reqMsg;
    }
    public void run() {
    	try {
    	
    		NnBean.RspMsg rspMsg=NnBean.RspMsg.newBuilder().build();
            
            int msgType=reqMsg.getMsgType();
            if(msgType!=0){
        		boolean isAuth = CommUtil.authcheck(reqMsg.getToken(),reqMsg.getUserId());
                if(!isAuth){
                	rspMsg=NnBean.RspMsg.newBuilder()
                			.setCode(NnRspCodeEnum.$0001.getCode())
                			.setMsg(NnRspCodeEnum.$0001.getMsg()).build();
                	ctx.writeAndFlush(rspMsg);
                	return ;
                }
            	
            }
           if(!checkIsRepeatCommit(reqMsg.getUserId(), reqMsg.getTimestamp())){
        	   rspMsg=NnBean.RspMsg.newBuilder()
           			.setCode(NnRspCodeEnum.$0005.getCode())
           			.setMsg(NnRspCodeEnum.$0005.getMsg()).build();
        	   ctx.writeAndFlush(rspMsg);
        	   return;
           }
            
           NnManager(rspMsg, msgType);
            
		} catch (Exception e) {
			e.printStackTrace();
		}

    }
    
    
    private void NnManager(NnBean.RspMsg rspMsg, int msgType){
    	
    	try {
			
	         if(NnReqMsgTypeEnum.JOIN_ROOM.getCode()==msgType){//加入房间
	        		NnManager.joinRoom(reqMsg, ctx);
	        }else if(NnReqMsgTypeEnum.REDAY.getCode()==msgType){//准备
	        		NnManager.readyMatch(reqMsg,ctx);
	        }else if(NnReqMsgTypeEnum.ROBBERY_LANDLORD.getCode()==msgType){//抢庄
	        		NnManager.grabLandlord(reqMsg,ctx);
	        }else if(NnReqMsgTypeEnum.ADD_MULTIPLE.getCode()==msgType){//闲家加倍
	        		NnManager.setFarmerDouble(reqMsg,ctx);
	        }else if(NnReqMsgTypeEnum.SHOW_MATCH_RESULT.getCode()==msgType){//明牌
	        		NnManager.setIsShowCard(reqMsg,ctx);
	        }else if(NnReqMsgTypeEnum.INIT_ROOM.getCode()==msgType){//房间初始化
	        	NnManager.initRoom(reqMsg,ctx);
	        }else if(NnReqMsgTypeEnum.EXIT_ROOM.getCode()==msgType){//退出房间
	        	NnManager.exitRoom(reqMsg,ctx);
	       }else if(NnReqMsgTypeEnum.SEND_MSG.getCode()==msgType
	    		   ||NnReqMsgTypeEnum.SEND_APPOINT_VOICE.getCode()==msgType
	    		   ||NnReqMsgTypeEnum.SEND_VOICE.getCode()==msgType
	    		   ||NnReqMsgTypeEnum.SEND_EMOJI.getCode()==msgType){//发送消息
	    	   NnManager.sendTalkMsg(reqMsg);
	       }else if(NnReqMsgTypeEnum.SEND_PROP.getCode()==msgType){
	    	   NnManager.sendProp(reqMsg,ctx);
	       }
    	} catch (Exception e) {
		}
        
    }
    

	/**
	 * 检查是否是重复提交
	 * @param userId
	 * @return
	 */
	private static boolean checkIsRepeatCommit(long userId,long timestamp){

		boolean flag=RedisUtil.hexists(NnConstans.NN_USER_THREAD_LOCK_CACHE_PRE, userId+"");
		
		if(!flag)
			return true;
		
		String orgTimestamp=RedisUtil.hget(NnConstans.NN_USER_THREAD_LOCK_CACHE_PRE, userId+"");
		if(timestamp>=Long.parseLong(orgTimestamp)){
			RedisUtil.hset(NnConstans.NN_USER_THREAD_LOCK_CACHE_PRE, userId+"", System.currentTimeMillis()+"");
			return true;
		}
		return false;
	}
}
