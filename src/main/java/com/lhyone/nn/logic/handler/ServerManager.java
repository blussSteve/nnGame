package com.lhyone.nn.logic.handler;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lhyone.nn.pb.NnBean;
import com.lhyone.nn.util.NnConstans;
import com.lhyone.util.RedisUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ServerManager {
	 public final static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	 private static Logger logger = LogManager.getLogger(ServerManager.class);
	
	 /**
	  * @param reqMsg
	  * @param ctx
	  */
	 public static void initChannel(NnBean.ReqMsg reqMsg,ChannelHandlerContext ctx){
		if(!channels.contains(ctx.channel())){
			channels.add(ctx.channel());
		    RedisUtil.hset(NnConstans.NN_USER_CHANNEL_PRE,reqMsg.getUserId()+"" , ctx.channel().id().asLongText());
		}
	 }
	 /**
	  * 删除渠道
	  * @param reqMsg
	  * @param ctx
	  */
	 public static void delChannel(ChannelHandlerContext ctx){
		 
		 if(channels.contains(ctx.channel())){
			channels.remove(ctx.channel());
		 }
	 }
	 
	 /**
	  * 删除渠道
	  * @param reqMsg
	  * @param ctx
	  */
	 public static void delChannel(ChannelHandlerContext ctx,NnBean.ReqMsg reqMsg){
		 
		 if(channels.contains(ctx.channel())){
			RedisUtil.hdel(NnConstans.NN_CHANNEL_PRE, ctx.channel().id().asLongText());
			RedisUtil.hdel(NnConstans.NN_USER_CHANNEL_PRE,reqMsg.getUserId()+"" );
		 }
	 }
	 /**推送单个消息*/
	 public static void pushsingle(NnBean.RspMsg rspMsg,ChannelHandlerContext ctx){
		 Channel channel= channels.find(ctx.channel().id());
		 channel.writeAndFlush(rspMsg);
	 }
   /**
	 * 多人发送消息
	 * @param map
	 */
	public static void batchSendMsg(Map<String,NnBean.RspMsg> map){
		try{
			for(String key:map.keySet()){
			  for(Channel channel:channels){
				 if(channel.id().asLongText().equalsIgnoreCase(key)){	
					logger.info("推送channeId:"+channel.id().asLongText()+":操作类型="+map.get(key).getOperateType()+":推送信息长度="+map.get(key).toByteArray().length);
					channel.writeAndFlush(map.get(key));
					break;
				}
			  }
		}
	}catch(Exception ce){
		ce.printStackTrace();
	}
	}
}
