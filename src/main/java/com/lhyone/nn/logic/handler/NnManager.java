package com.lhyone.nn.logic.handler;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.lhyone.nn.dao.NnManagerDao;
import com.lhyone.nn.entity.BugUser;
import com.lhyone.nn.entity.GoldRecord;
import com.lhyone.nn.entity.JoinRoomUser;
import com.lhyone.nn.entity.NnRoom;
import com.lhyone.nn.entity.NnRoomMatchUser;
import com.lhyone.nn.entity.UserCostRecord;
import com.lhyone.nn.entity.UserMatchRecord;
import com.lhyone.nn.enums.GameTypeEnum;
import com.lhyone.nn.enums.GoldTypeEnum;
import com.lhyone.nn.enums.NnCardTypeTransEnum;
import com.lhyone.nn.enums.NnPushMsgTypeEnum;
import com.lhyone.nn.enums.NnReqMsgTypeEnum;
import com.lhyone.nn.enums.NnRoomMatchStatusEnum;
import com.lhyone.nn.enums.NnRspCodeEnum;
import com.lhyone.nn.enums.NnRspMsgTypeEnum;
import com.lhyone.nn.enums.NnTalkTypeEnum;
import com.lhyone.nn.enums.NnTimeTaskEnum;
import com.lhyone.nn.enums.NnUserRoleEnum;
import com.lhyone.nn.enums.NnWrokEnum;
import com.lhyone.nn.enums.NnYesNoEnum;
import com.lhyone.nn.enums.NumEnum;
import com.lhyone.nn.pb.NnBean;
import com.lhyone.nn.pb.NnBean.ReqMsg;
import com.lhyone.nn.util.LocalCacheUtil;
import com.lhyone.nn.util.NnCardUtil;
import com.lhyone.nn.util.NnConstans;
import com.lhyone.nn.util.NnUtil;
import com.lhyone.nn.vo.DbVo;
import com.lhyone.nn.vo.GameTimoutVo;
import com.lhyone.nn.vo.NnRoomUserTalkVo;
import com.lhyone.nn.vo.PropDicVo;
import com.lhyone.nn.vo.UserCacheVo;
import com.lhyone.util.RedisUtil;
import com.vdurmont.emoji.EmojiParser;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

public class NnManager {
	private static Logger logger = LogManager.getLogger(NnManager.class);

	/**
	 * @param reqMsg
	 * @param ctx
	 */
	public static void initChannel(NnBean.ReqMsg reqMsg, ChannelHandlerContext ctx) {
		if (!ServerManager.channels.contains(ctx.channel())) {
			ServerManager.channels.add(ctx.channel());
			RedisUtil.hset(NnConstans.NN_USER_CHANNEL_PRE, reqMsg.getUserId() + "", ctx.channel().id().asLongText());
		}
	}

	/**
	 * 删除渠道
	 * 
	 * @param reqMsg
	 * @param ctx
	 */
	public static void delChannel(ChannelHandlerContext ctx) {

		if (ServerManager.channels.contains(ctx.channel())) {
			ServerManager.channels.remove(ctx.channel());
		}
	}

	/**
	 * 删除渠道
	 * 
	 * @param reqMsg
	 * @param ctx
	 */
	public static void delChannel(ChannelHandlerContext ctx, NnBean.ReqMsg reqMsg) {

		if (ServerManager.channels.contains(ctx.channel())) {
			RedisUtil.hdel(NnConstans.NN_CHANNEL_PRE, ctx.channel().id().asLongText());
			RedisUtil.hdel(NnConstans.NN_USER_CHANNEL_PRE, reqMsg.getUserId() + "");
		}
	}

	/** 推送单个消息 */
	public static void pushsingle(NnBean.RspMsg rspMsg, ChannelHandlerContext ctx) {
		System.out.println();
		logger.info("推送单个信息开是,推送channeId:{},操作类型:{},信息长度为:{},响应码为：{}", ctx.channel().id().asLongText(), rspMsg.getOperateType(), rspMsg.toString().length(), rspMsg.getCode());
		Channel channel = ServerManager.channels.find(ctx.channel().id());
		if (!channel.isActive() && !channel.isOpen()) {
			channel.close();
		} else {
			channel.writeAndFlush(rspMsg);
		}
	}

	//

	/**
	 * 赠送礼物
	 * 
	 * @param reqMsg
	 * @param ctx
	 * @return
	 */
	public static void sendProp(NnBean.ReqMsg reqMsg, ChannelHandlerContext ctx) {
		logger.info("赠送礼物开始....");
		NnBean.RspMsg.Builder rspMsg = NnBean.RspMsg.newBuilder();
		// 加个用户操作缓存锁
		if (RedisUtil.hexists(NnConstans.NN_USER_LOCK_REQ, reqMsg.getUserId() + "")) {
			rspMsg.setCode(NnRspCodeEnum.$0006.getCode());
			rspMsg.setMsg(NnRspCodeEnum.$0006.getMsg());
			rspMsg.setOperateType(NnRspMsgTypeEnum.SEND_PROP_FEEDBACK.getCode());
			pushsingle(rspMsg.build(), ctx);
			logger.info("请勿重复提交");
			return;
		}

		rspMsg.setCode(NnRspCodeEnum.$0000.getCode());
		rspMsg.setMsg(NnRspCodeEnum.$0000.getMsg());
		rspMsg.setOperateType(NnRspMsgTypeEnum.SEND_PROP_FEEDBACK.getCode());

		PropDicVo prop = NnUtil.getPropDicVoByNum(reqMsg.getPropNum());

		if (null == prop || reqMsg.getBeSendPropUserId() == 0) {

			rspMsg.setCode(NnRspCodeEnum.$0004.getCode());
			rspMsg.setMsg(NnRspCodeEnum.$0004.getMsg());
			rspMsg.setOperateType(NnRspMsgTypeEnum.SEND_PROP_FEEDBACK.getCode());

			pushsingle(rspMsg.build(), ctx);
			return;
		}

		// 判断道具卡是否充足

		long gold = NnManagerDao.instance().getUserGold(reqMsg.getUserId());
		if (gold < prop.getCostGold()) {
			rspMsg.setCode(NnRspCodeEnum.$1120.getCode());
			rspMsg.setMsg(NnRspCodeEnum.$1120.getMsg());
			rspMsg.setOperateType(NnRspMsgTypeEnum.SEND_PROP_FEEDBACK.getCode());
			pushsingle(rspMsg.build(), ctx);
			return;
		}
		//

		NnBean.PropInfo.Builder userProp = NnBean.PropInfo.newBuilder();
		userProp.setUserId(reqMsg.getUserId());
		userProp.setPosition(getCurUser(reqMsg.getUserId(), reqMsg.getRoomNo()).getPosition());

		userProp.setBeUserId(reqMsg.getBeSendPropUserId());
		userProp.setBePosition(getCurUser(reqMsg.getBeSendPropUserId(), reqMsg.getRoomNo()).getPosition());
		userProp.setPropNum(prop.getNum());
		NnBean.RspData.Builder rspData = NnBean.RspData.newBuilder();
		rspData.setProp(userProp);
		rspMsg.setData(rspData);

		Set<String> set = getAllUserSet(reqMsg.getRoomNo());
		Map<String, NnBean.RspMsg> map = new HashMap<String, NnBean.RspMsg>();
		for (String key : set) {
			rspMsg.setOperateType(NnPushMsgTypeEnum.SEND_PROP_PUSH.getCode());
			map.put(RedisUtil.hget(NnConstans.NN_USER_CHANNEL_PRE, key), rspMsg.build());
		}
		batchSendMsg(map);
		NnManagerDao.instance().sendUserProp(reqMsg.getUserId(), reqMsg.getBeSendPropUserId(), prop);
		logger.info("赠送礼物结束....");
	}

	/**
	 * 加入房间
	 * 
	 * @param reqMsg
	 * @return
	 * @throws Exception
	 */
	public static void joinRoom(NnBean.ReqMsg reqMsg, ChannelHandlerContext ctx) throws Exception {
		logger.info("加入房间开始...");
		NnBean.RspMsg.Builder rspMsg = NnBean.RspMsg.newBuilder();

		rspMsg.setOperateType(NnRspMsgTypeEnum.JOIN_ROOM_FEEDBACK.getCode());
		NnBean.RspData.Builder rspData = NnBean.RspData.newBuilder();
		Jedis redis = RedisUtil.getJedis();
		Transaction tx = redis.multi();

		try {
			
			if (!RedisUtil.exists(NnConstans.NN_ROOM_PRE + reqMsg.getRoomNo())) {
				rspMsg.setCode(NnRspCodeEnum.$1001.getCode());
				rspMsg.setMsg(NnRspCodeEnum.$1001.getMsg());
				logger.info(NnRspCodeEnum.$1001.getMsg());
				pushsingle(rspMsg.build(), ctx);
				return;
			}
			
			// 判断是否在房间内
			long i = NnManagerDao.instance().checkNnUserIsJoinRoom(reqMsg.getUserId());

			if (i > 0) {
				rspMsg.setCode(NnRspCodeEnum.$1002.getCode());
				rspMsg.setMsg(NnRspCodeEnum.$1002.getMsg());
				logger.info(NnRspCodeEnum.$1002.getMsg());
				pushsingle(rspMsg.build(), ctx);
				return;
			}
			// 增加房间用户
			JoinRoomUser njru = new JoinRoomUser();
			njru.setUserId(reqMsg.getUserId());
			njru.setRoomId(getRoomId(reqMsg.getRoomNo()));
			njru.setRoomNo(reqMsg.getRoomNo());
			njru.setGameType(GameTypeEnum.NIU_NIU.getType());
			njru.setStatus(2);// 默认值
			njru.setCreateDate(new Date());

			NnManagerDao.instance().addNnRoomUser(njru);

			// 3.判断房间是否已经开赛
			NnBean.RoomInfo.Builder roomInfo = getRoomInfo(reqMsg.getRoomNo());

			NnRoom nnRoomVo = getRoomVo(reqMsg.getRoomNo());

			// 6.用户加入房间
			NnBean.UserInfo.Builder uinfo = NnBean.UserInfo.newBuilder();
			UserCacheVo uv = getUserInfo(reqMsg.getUserId());
			uinfo.setUserId(reqMsg.getUserId());
			uinfo.setNickName(uv.getUserName());//
			uinfo.setHeadUrl(uv.getHeadImgUrl());//
			uinfo.setGender(uv.getGender());
			uinfo.setMark(uv.getMark());
			uinfo.setPosition(getPostion(reqMsg.getUserId(), reqMsg.getRoomNo()));// 设置位置信息
			uinfo.setRoomNo(reqMsg.getRoomNo());
			uinfo.setBaseScore(nnRoomVo.getBaseGold());// 设置底分
			uinfo.setUserGold((int) NnManagerDao.instance().getUserGold(reqMsg.getUserId()));
			uinfo.setTotalGold(uinfo.getUserGold());
			uinfo.setIp(ctx.channel().remoteAddress().toString().replaceAll("(/)|:(.*)", ""));

			if (roomInfo.getRoomCurMatchStatus() == 1) {
				uinfo.setPlayerType(NnUserRoleEnum.GUEST.getCode());// 设置用角色为游客
			}
			// 增加准备倒计时
			{
				GameTimoutVo timeVo=new GameTimoutVo();
				timeVo.setRestTime(System.currentTimeMillis());
				timeVo.setRestTimeType(NnTimeTaskEnum.USER_REDAY.getCode());
				RedisUtil.hset(NnConstans.NN_REST_TIME_PRE+ reqMsg.getRoomNo(),reqMsg.getUserId()+"", JSONObject.toJSONString(timeVo));
				long userRedayTime = timeVo.getRestTime();
				ServerManager.executorTask.schedule(new MyTimerTask(reqMsg, NnTimeTaskEnum.USER_REDAY.getCode(),timeVo.getRestTime()), NnConstans.USER_RDAY_TIME, TimeUnit.SECONDS);
				userRedayTime = getRedayTime(reqMsg.getRoomNo(), String.valueOf(reqMsg.getUserId()));
				uinfo.setRedayTime((int) userRedayTime);
			}

			rspData.setRestTime(NnConstans.USER_RDAY_TIME);

			// 增加房间用户信息缓存
			tx.hset(NnConstans.NN_ROOM_USER_INFO_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "", JsonFormat.printToString(uinfo.build()));

			// 增加所有用户
			tx.sadd(NnConstans.NN_ROOM_ALL_USER_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "");// 牛牛所有用户

			// 8.更新用户房间缓存信息

			roomInfo.setRoomCurPersonCount(roomInfo.getRoomCurPersonCount() + 1);// 设置房间当前人数

			if (roomInfo.getRoomMaxPersonCount() <= roomInfo.getRoomCurPersonCount()) {

				roomInfo.setRoomCurStatus(NnYesNoEnum.YES.getCode());// 当前人数已满
			}

			tx.hset(NnConstans.NN_ROOM_PRE + roomInfo.getRoomNo(), "roomInfo", JsonFormat.printToString(roomInfo.build()));

			tx.exec();// 执行事务提交
			// 9.封装返回结果信息

			/** 用户无用信息清除 */
			{
				rspData.setUser(uinfo);
			}

			Set<String> set = getAllUserSet(reqMsg.getRoomNo());

			for (String key : set) {
				if (Long.parseLong(key) != reqMsg.getUserId()) {
					rspData.addOtherUser(getCurUser(Long.parseLong(key), reqMsg.getRoomNo()));
				}
			}

			String curStatus = RedisUtil.hget(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo());
			int restTime = getRestTime(reqMsg.getRoomNo());
			/** 房间无用信息清除 */
			{
				roomInfo.clearCardDouble();
				rspData.setCurRoomStatus(Integer.parseInt(curStatus));
				rspData.setRestTime(restTime);
				rspData.setRoom(roomInfo);// 增加返回对象

			}

			rspMsg.setData(rspData);
			rspMsg.setOperateType(NnRspMsgTypeEnum.JOIN_ROOM_FEEDBACK.getCode());
			rspMsg.setCode(NnRspCodeEnum.$0000.getCode());
			rspMsg.setMsg(NnRspCodeEnum.$0000.getMsg());

			pushsingle(rspMsg.build(), ctx);// 推送单个用户

			batchSendJoinRoom(reqMsg);// 批量加入房间推送
			logger.info("加入房间结束...");
		} catch (Exception e) {
			NnManagerDao.instance().deleteRoomUser(reqMsg.getUserId(), reqMsg.getRoomNo(), GameTypeEnum.NIU_NIU.getType());
			logger.error(e.getMessage(), e);
			e.printStackTrace();
			rspMsg.setOperateType(NnRspMsgTypeEnum.JOIN_ROOM_FEEDBACK.getCode());
			rspMsg.setCode(NnRspCodeEnum.$9999.getCode());
			rspMsg.setMsg(NnRspCodeEnum.$9999.getMsg());
			pushsingle(rspMsg.build(), ctx);
		} finally {
			redis.close();
		}
	}

	/**
	 * 获取剩余时间
	 * 
	 * @param reqMsg
	 * @return
	 */
	private static int getRestTime(String roomNo) {
		String sysTimeStr=RedisUtil.get(NnConstans.NN_SYS_CUR_TIME_CACHE);
		long sysTime=Long.parseLong(sysTimeStr);
		int restTime = 0;
		GameTimoutVo timeOutVo = getGameTimoutVo(roomNo);
		if (timeOutVo.getRestTimeType()==NnTimeTaskEnum.GRAB_LANDLORD.getCode()) {
			restTime = (int) (NnConstans.GRAB_LANDLORD_TIME - (sysTime-timeOutVo.getRestTime()) / 1000);
		}
		if (timeOutVo.getRestTimeType()==NnTimeTaskEnum.FARMER_DOUBLEX.getCode()) {
			restTime = (int) (NnConstans.FARMER_TIME - (sysTime- timeOutVo.getRestTime()) / 1000);
		}
		if (timeOutVo.getRestTimeType()==NnTimeTaskEnum.SHOW_CARD_RESULT.getCode()) {
			restTime = (int) (NnConstans.SHOW_MATCH_TIME - (sysTime-timeOutVo.getRestTime()) / 1000);
		}
		return restTime;
	}

	/**
	 * 获取准备倒计时
	 * 
	 * @param reqMsg
	 * @return
	 */
	private static int getRedayTime(String roomNo, String userId) {
		String sysTimeStr=RedisUtil.get(NnConstans.NN_SYS_CUR_TIME_CACHE);
		long sysTime=Long.parseLong(sysTimeStr);
		int redayTime = 0;
		if (RedisUtil.hexists(NnConstans.NN_REST_TIME_PRE + roomNo, userId)) {
			String str = RedisUtil.hget(NnConstans.NN_REST_TIME_PRE + roomNo, userId);
			GameTimoutVo time=JSONObject.parseObject(str, GameTimoutVo.class);
			redayTime = (int) (NnConstans.USER_RDAY_TIME - (sysTime -time.getRestTime()) / 1000);
			if (redayTime <= 0) {
				redayTime = 0;
			}
		}
		return redayTime;
	}
	private static GameTimoutVo getUserTimeVo(String roomNo,String userId){
		String str = RedisUtil.hget(NnConstans.NN_REST_TIME_PRE + roomNo, userId);
		if(StringUtils.isEmpty(str)){
			return null;
		}
		return JSONObject.parseObject(str, GameTimoutVo.class);
	}
	
	/**
	 * 批量加入房间推送
	 * 
	 * @param reqMsg
	 */
	private static void batchSendJoinRoom(ReqMsg reqMsg) {
		String roomNo = reqMsg.getRoomNo();
		Set<String> userSet = getAllUserSet(reqMsg.getRoomNo());

		String curStatus = RedisUtil.hget(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo());

		NnBean.UserInfo.Builder userInfo = getCurUser(reqMsg.getUserId(), roomNo);

		userInfo.clearToken();
		userInfo.clearRoomNo();
		userInfo.clearIsreday();
		userInfo.clearIsGrabLandlord();
		userInfo.clearScoreType();
		userInfo.clearCurMatchGold();
		NnBean.RoomInfo.Builder roomInfo = getRoomInfo(roomNo);

		Map<String, NnBean.RspMsg> map = new HashMap<String, NnBean.RspMsg>();

		NnBean.RspMsg.Builder rspMsg = NnBean.RspMsg.newBuilder();

		NnBean.RspData.Builder rspData = NnBean.RspData.newBuilder();

		if (userInfo.getIsreday() == NnYesNoEnum.NO.getCode()) {
			userInfo.setRedayTime(getRedayTime(reqMsg.getRoomNo(), String.valueOf(userInfo.getUserId())));
		}

		rspData.setRestTime(getRestTime(reqMsg.getRoomNo()));

		rspData.setUser(userInfo);
		rspData.setCurRoomStatus(Integer.parseInt(curStatus));
		rspData.setRoom(roomInfo);
		rspMsg.setOperateType(NnPushMsgTypeEnum.JOIN_ROOM_PUSH.getCode());
		rspMsg.setData(rspData);
		rspMsg.setCode(NnRspCodeEnum.$0000.getCode());
		rspMsg.setMsg(NnRspCodeEnum.$0000.getMsg());

		for (String key : userSet) {

			if (reqMsg.getUserId() != Long.parseLong(key)) {
				map.put(RedisUtil.hget(NnConstans.NN_USER_CHANNEL_PRE, key), rspMsg.build());
			}

		}
		batchSendMsg(map);
	}

	/**
	 * 
	 * 
	 * @return
	 */
	private static UserCacheVo getUserInfo(long userId) {
		try{
			String	str=RedisUtil.get(NnConstans.REDIS_USER_PRE + userId);
			UserCacheVo uv=	JSONObject.parseObject(str, UserCacheVo.class);
			return uv;
		}catch (Exception e) {
			e.printStackTrace();
			return new UserCacheVo();
		}
		
	}

	/**
	 * 获所有用户集合
	 * 
	 * @param roomNo
	 * @return
	 */
	public static Set<String> getAllUserSet(String roomNo) {
		return RedisUtil.smembers(NnConstans.NN_ROOM_ALL_USER_PRE + roomNo);
	}

	/**
	 * 获取房间所有准备用户集合
	 * 
	 * @param roomNo
	 * @return
	 */
	public static Set<String> getAllRedayUserSet(String roomNo) {
		return RedisUtil.smembers(NnConstans.NN_ROOM_ALL_READY_USER_PRE + roomNo);
	}

	/**
	 * 获取房间用户集合
	 * 
	 * @param roomNo
	 * @return
	 */
	public static Set<String> getAllMatchUserSet(String roomNo) {
		return RedisUtil.smembers(NnConstans.NN_ROOM_ALL_MATCH_USER_PRE + roomNo);
	}

	/**
	 * 获取用户信息
	 * 
	 * @param userId
	 * @return
	 */
	public static NnBean.UserInfo.Builder getCurUser(long userId, String roomNo) {
		NnBean.UserInfo.Builder user = NnBean.UserInfo.newBuilder();
		try {
			JsonFormat.merge(RedisUtil.hget(NnConstans.NN_ROOM_USER_INFO_PRE + roomNo, userId + ""), user);
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return user;
	}

	/**
	 * 获取房间信息
	 * 
	 * @param roomNo
	 * @return
	 */
	public static NnBean.RoomInfo.Builder getRoomInfo(String roomNo) {
		NnBean.RoomInfo.Builder roomInfo = NnBean.RoomInfo.newBuilder();
		if (!StringUtils.isEmpty(roomNo)) {
			String roomStr = RedisUtil.hget(NnConstans.NN_ROOM_PRE + roomNo, "roomInfo");
			try {
				JsonFormat.merge(roomStr, roomInfo);
			} catch (ParseException e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
		}
		return roomInfo;
	}

	/**
	 * 获取房间信息
	 * 
	 * @param roomNo
	 * @return
	 */
	private static long getRoomId(String roomNo) {
		String str = RedisUtil.hget(NnConstans.NN_ROOM_PRE + roomNo, "roomVo");

		if (StringUtils.isNoneEmpty(str)) {

			NnRoom nnRoomVo = (NnRoom) (JSONObject.parseObject(str, NnRoom.class));

			return nnRoomVo.getId();
		}

		return 0;
	}

	/**
	 * 获取房间信息
	 * 
	 * @param roomNo
	 * @return
	 */
	private static NnRoom getRoomVo(String roomNo) {
		String str = RedisUtil.hget(NnConstans.NN_ROOM_PRE + roomNo, "roomVo");

		if (StringUtils.isNoneEmpty(str)) {

			NnRoom nnRoomVo = (NnRoom) (JSONObject.parseObject(str, NnRoom.class));

			return nnRoomVo;
		}
		return new NnRoom();
	}

	/**
	 * 准备比赛
	 * 
	 * @param reqMsg
	 * @return
	 * @throws Exception
	 */
	public static void readyMatch(NnBean.ReqMsg reqMsg, ChannelHandlerContext ctx) throws Exception {

		NnBean.RspMsg.Builder rspMsg = NnBean.RspMsg.newBuilder();

		// 1.判断用户是否在该房间中

		boolean isInRoom = RedisUtil.sismember(NnConstans.NN_ROOM_ALL_USER_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "");
		if (!isInRoom) {
			rspMsg.setOperateType(NnRspMsgTypeEnum.REDAY_ROOM_FEEDBACK.getCode());
			rspMsg.setCode(NnRspCodeEnum.$1102.getCode());
			rspMsg.setMsg(NnRspCodeEnum.$1102.getMsg());
			NnManager.pushsingle(rspMsg.build(), ctx);

			return;

		}
		// 2.更改用户状态

		NnBean.UserInfo.Builder userInfo = getCurUser(reqMsg.getUserId(), reqMsg.getRoomNo());

		if (userInfo.getIsreday() == NnYesNoEnum.YES.getCode()) {// 如果已准备
			rspMsg.setOperateType(NnRspMsgTypeEnum.REDAY_ROOM_FEEDBACK.getCode());
			rspMsg.setCode(NnRspCodeEnum.$1113.getCode());
			rspMsg.setMsg(NnRspCodeEnum.$1113.getMsg());
			NnManager.pushsingle(rspMsg.build(), ctx);
			return;

		}
		userInfo.clearToken();
		userInfo.clearRoomNo();
		userInfo.clearIsWin();
		userInfo.clearIsGrabLandlord();
		userInfo.clearIsGrabFarmer();
		userInfo.clearScoreType();
		userInfo.clearCard();
		userInfo.clearCardCount();
		userInfo.clearIsShowCard();
		userInfo.clearCurMatchGold();
		userInfo.clearPlayerType();
		userInfo.setIsreday(NnYesNoEnum.YES.getCode());// 更新用户状态为准备状态

		if (userInfo.getPlayerType() == NnUserRoleEnum.GUEST.getCode()) {
			userInfo.setPlayerType(NnUserRoleEnum.MATCH.getCode());
		}
		NnBean.RoomInfo.Builder nnRoom = getRoomInfo(reqMsg.getRoomNo());

		if (nnRoom.getRoomCurMatchStatus() != NumEnum.ONE.getNumInteger()) {// 如果没有比赛
			RedisUtil.hset(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo(), NnRoomMatchStatusEnum.REDAY_STATUS.getCode() + "");// 设置当前为用户准备状态
		}

		// 清除准备倒计时
		RedisUtil.hdel(NnConstans.NN_REST_TIME_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "");
		// 增加准备用户
		RedisUtil.sadd(NnConstans.NN_ROOM_ALL_READY_USER_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "");// 设置房间用户
		// 更新用户缓存
		RedisUtil.hset(NnConstans.NN_ROOM_USER_INFO_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "", JsonFormat.printToString(userInfo.build()));// 更新缓存

		rspMsg.setOperateType(NnRspMsgTypeEnum.REDAY_ROOM_FEEDBACK.getCode());
		rspMsg.setCode(NnRspCodeEnum.$0000.getCode());
		rspMsg.setMsg(NnRspCodeEnum.$0000.getMsg());
		NnManager.pushsingle(rspMsg.build(), ctx);

		batchSendReday(reqMsg);// 用户准备推送

		// 判断是否发牌

		Set<String> allUser = getAllUserSet(reqMsg.getRoomNo());// 所有用户
		Set<String> allReadyUser = getAllRedayUserSet(reqMsg.getRoomNo());// 所有准备用户
		// 如果全部准备直接进行发牌
		if (allUser.size() <= allReadyUser.size() && allUser.size() > 1) {

			// 设置房间当前比赛场数
			nnRoom.setRoomCurMatchCount(nnRoom.getRoomCurMatchCount() + 1);
			RedisUtil.hset(NnConstans.NN_ROOM_PRE + reqMsg.getRoomNo(), "roomInfo", JsonFormat.printToString(nnRoom.build()));

			ServerManager.executor.execute(new NnWork(reqMsg, NnWrokEnum.SEND_CARD_RESULT.getCode(), ctx));
		}

	}

	/**
	 * 用户准备推送
	 * 
	 * @param reqMsg
	 */
	public static void batchSendReday(ReqMsg reqMsg) {
		String roomNo = reqMsg.getRoomNo();
		Set<String> userSet = getAllUserSet(reqMsg.getRoomNo());

		String curStatus = RedisUtil.hget(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo());

		NnBean.UserInfo.Builder userInfo = getCurUser(reqMsg.getUserId(), roomNo);
		userInfo.clearToken();
		userInfo.clearRoomNo();
		userInfo.clearIsWin();
		userInfo.clearIsreday();
		userInfo.clearIsGrabLandlord();
		userInfo.clearScoreType();
		userInfo.clearCard();
		Map<String, NnBean.RspMsg> map = new HashMap<String, NnBean.RspMsg>();

		NnBean.RspMsg.Builder rspMsg = NnBean.RspMsg.newBuilder();

		NnBean.RspData.Builder rspData = NnBean.RspData.newBuilder();

		rspData.setRestTime(getRestTime(reqMsg.getRoomNo()));

		rspData.setUser(userInfo);
		rspData.setCurRoomStatus(Integer.parseInt(curStatus));
		rspMsg.setOperateType(NnPushMsgTypeEnum.REDAY_ROOM_PUSH.getCode());
		rspMsg.setData(rspData);
		rspMsg.setCode(NnRspCodeEnum.$0000.getCode());
		rspMsg.setMsg(NnRspCodeEnum.$0000.getMsg());

		for (String key : userSet) {

			if (reqMsg.getUserId() != Long.parseLong(key)) {
				map.put(RedisUtil.hget(NnConstans.NN_USER_CHANNEL_PRE, key), rspMsg.build());
			}

		}
		batchSendMsg(map);
	}

	/**
	 * 准备发牌
	 * 
	 * @param reqMsg
	 */
	public static void redaySendCard(NnBean.ReqMsg reqMsg) {

		if (RedisUtil.hexists(NnConstans.NN_ROOM_LOCK_REQ, reqMsg.getRoomNo())) {
			logger.info("重复发牌操作...............................");
			return;
		}
		try {

			
			// 设置定时器，增加自动抢庄机制
			{
				GameTimoutVo timeout = new GameTimoutVo();
				timeout.setRestTime(System.currentTimeMillis());
				timeout.setRestTimeType(NnTimeTaskEnum.GRAB_LANDLORD.getCode());

				RedisUtil.hset(NnConstans.NN_REST_TIME_PRE, reqMsg.getRoomNo(), JSONObject.toJSONString(timeout));
				ServerManager.executorTask.schedule(new MyTimerTask(reqMsg, NnTimeTaskEnum.GRAB_LANDLORD.getCode(),timeout.getRestTime()), NnConstans.GRAB_LANDLORD_TIME, TimeUnit.SECONDS);

			}

			// 增加房间锁，防止重复提交
			RedisUtil.hset(NnConstans.NN_ROOM_LOCK_REQ, reqMsg.getRoomNo(), System.currentTimeMillis() + "");

			// 清楚卡缓存
			RedisUtil.hdel(NnConstans.NN_ROOM_USER_ALL_CARD_PRE, reqMsg.getRoomNo());
			// 清除抢庄缓存
			RedisUtil.del(NnConstans.NN_ROOM_LANDLORD_USER_PRE + reqMsg.getRoomNo());
			// 清除闲家设置翻倍缓存
			RedisUtil.del(NnConstans.NN_ROOM_FARMER_USER_PRE + reqMsg.getRoomNo());

			// 设置房间状态
			NnBean.RoomInfo.Builder roomInfo = getRoomInfo(reqMsg.getRoomNo());
			roomInfo.setRoomCurMatchStatus(NnYesNoEnum.YES.getCode());
			RedisUtil.hset(NnConstans.NN_ROOM_PRE + reqMsg.getRoomNo(), "roomInfo", JsonFormat.printToString(roomInfo.build()));

			final Set<String> userSet = getAllRedayUserSet(reqMsg.getRoomNo());
			// 发牌
			Map<String, NnBean.UserInfo> userMap = new HashMap<String, NnBean.UserInfo>();
			String[] sers = new String[userSet.size()];
			StringBuffer ids = new StringBuffer();
			int i = 0;
			for (String key : userSet) {
				ids.append(key + "");
				sers[i] = key;
				NnBean.UserInfo.Builder user = getCurUser(Long.parseLong(key), reqMsg.getRoomNo());
				userMap.put(key, user.build());
				i++;
				// 增加比赛用户
				RedisUtil.sadd(NnConstans.NN_ROOM_ALL_MATCH_USER_PRE + reqMsg.getRoomNo(), key);
				// 去掉准备用户
				RedisUtil.srem(NnConstans.NN_ROOM_ALL_READY_USER_PRE + reqMsg.getRoomNo(), key);

			}

			RedisUtil.del(NnConstans.NN_BUG_USER_PRE + reqMsg.getRoomNo());
			Map<String, List<Integer>> userCardMap = null;
			BugUser bugUser = NnManagerDao.instance().getBugUser(ids.substring(0, ids.length() - 1));
			if (bugUser != null) {
				RedisUtil.hset(NnConstans.NN_BUG_USER_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "", System.currentTimeMillis() + "");
				userCardMap = NnCardUtil.getCard(bugUser.getBugType(), bugUser.getUserId().toString(), bugUser.getWinRate(), sers);
			} else {
				userCardMap = NnCardUtil.getSimpleCard(sers);
			}

			for (String key : userCardMap.keySet()) {

				if (userMap.containsKey(key)) {

					NnBean.UserInfo.Builder user = NnBean.UserInfo.newBuilder(userMap.get(key));

					NnBean.CardInfo.Builder cardInfo = NnBean.CardInfo.newBuilder();
					List<Integer> cardList = userCardMap.get(key);
					cardInfo.addAllNum(cardList.subList(0, 4));
					user.setCard(cardInfo);
					user.setCardCount(cardInfo.getNumList().size());

					NnManagerDao.instance().subUserGold(user.getUserId(), roomInfo.getCostGold());
					long userGold = NnManagerDao.instance().getUserGold(user.getUserId());
					user.setUserGold((int) userGold);
					// 设置缓存
					RedisUtil.hset(NnConstans.NN_ROOM_USER_INFO_PRE + reqMsg.getRoomNo(), key, JsonFormat.printToString(user.build()));// 更新缓存

				}

			}

			// 设置生成的卡缓存
			RedisUtil.hset(NnConstans.NN_ROOM_USER_ALL_CARD_PRE, reqMsg.getRoomNo(), JSONObject.toJSONString(userCardMap));

			RedisUtil.hset(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo(), NnRoomMatchStatusEnum.GRAB_LANDLORD_STATUS.getCode() + "");

			
		
			batchSendCard(reqMsg);

			

		} finally {
			RedisUtil.hdel(NnConstans.NN_ROOM_LOCK_REQ, reqMsg.getRoomNo());
		}

	}

	/**
	 * 用户发牌批量推送
	 * 
	 * @param reqMsg
	 */
	private static void batchSendCard(ReqMsg reqMsg) {

		String roomNo = reqMsg.getRoomNo();
		Set<String> allUserSet = getAllUserSet(roomNo);
		Set<String> allMatchUserSet = getAllMatchUserSet(roomNo);

		String curStatus = RedisUtil.hget(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo());

		Map<String, NnBean.RspMsg> map = new HashMap<String, NnBean.RspMsg>();

		for (String key : allUserSet) {

			NnBean.UserInfo.Builder userInfo = getCurUser(Long.parseLong(key), roomNo);

			NnBean.RspMsg.Builder rspMsg = NnBean.RspMsg.newBuilder();

			NnBean.RspData.Builder rspData = NnBean.RspData.newBuilder();

			rspData.setRestTime(getRestTime(reqMsg.getRoomNo()));

			rspData.setUser(userInfo);
			rspData.setCurRoomStatus(Integer.parseInt(curStatus));

			List<NnBean.UserInfo> listOtherUser = new ArrayList<NnBean.UserInfo>();
			for (String matchKey : allMatchUserSet) {

				if (!key.equals(matchKey)) {
					NnBean.UserInfo.Builder otherUser = getCurUser(Long.parseLong(matchKey), roomNo);
					otherUser.clearToken();
					otherUser.clearRoomNo();
					otherUser.clearIsWin();
					otherUser.clearIsreday();
					otherUser.clearIsGrabLandlord();
					otherUser.clearCard();
					listOtherUser.add(otherUser.build());
				}
			}
			rspData.addAllOtherUser(listOtherUser);
			rspMsg.setOperateType(NnPushMsgTypeEnum.SEND_CARD_PUSH.getCode());
			rspMsg.setData(rspData);
			rspMsg.setCode(NnRspCodeEnum.$0000.getCode());
			rspMsg.setMsg(NnRspCodeEnum.$0000.getMsg());

			map.put(RedisUtil.hget(NnConstans.NN_USER_CHANNEL_PRE, key), rspMsg.build());

		}
		batchSendMsg(map);
	}

	/**
	 * 抢庄
	 * 
	 * @param reqMsg
	 * @return
	 * @throws Exception
	 */
	public static void grabLandlord(NnBean.ReqMsg reqMsg, ChannelHandlerContext ctx) throws Exception {
		NnBean.RspMsg.Builder rspMsg = NnBean.RspMsg.newBuilder();
		try {

			// 禁止没有参加比赛的用户抢庄
			if (!RedisUtil.sismember(NnConstans.NN_ROOM_ALL_MATCH_USER_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "")) {
				logger.info("该用户尚未参加比赛{},{}", reqMsg.getUserId(), reqMsg.getRoomNo());
				rspMsg.setOperateType(NnRspMsgTypeEnum.GRAB_LANDLORD_FEEDBACK.getCode());
				rspMsg.setCode(NnRspCodeEnum.$0003.getCode());
				rspMsg.setMsg(NnRspCodeEnum.$0003.getMsg());
				NnManager.pushsingle(rspMsg.build(), ctx);
				return;
			}

			// 1.判断此时状态是否可以抢庄

			String curStaus = RedisUtil.hget(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo());

			if (Integer.parseInt(curStaus) != NnRoomMatchStatusEnum.GRAB_LANDLORD_STATUS.getCode()) {
				rspMsg.setOperateType(NnRspMsgTypeEnum.GRAB_LANDLORD_FEEDBACK.getCode());
				rspMsg.setCode(NnRspCodeEnum.$0003.getCode());
				rspMsg.setMsg(NnRspCodeEnum.$0003.getMsg());
				NnManager.pushsingle(rspMsg.build(), ctx);
				return;

			}

			// 2.判断用户是否已经抢过庄

			if (RedisUtil.hexists(NnConstans.NN_ROOM_LANDLORD_USER_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "")) {
				rspMsg.setOperateType(NnRspMsgTypeEnum.GRAB_LANDLORD_FEEDBACK.getCode());
				rspMsg.setCode(NnRspCodeEnum.$1107.getCode());
				rspMsg.setMsg(NnRspCodeEnum.$1107.getMsg());
				NnManager.pushsingle(rspMsg.build(), ctx);
				return;

			}

			// 翻倍
			if (!NnUtil.checkLandlordScoreIsTrue(reqMsg.getScoreType()) || !NnYesNoEnum.isNull(reqMsg.getIsGrabLandLord())) {
				rspMsg.setOperateType(NnRspMsgTypeEnum.GRAB_LANDLORD_FEEDBACK.getCode());
				rspMsg.setCode(NnRspCodeEnum.$0003.getCode());
				rspMsg.setMsg(NnRspCodeEnum.$0003.getMsg());
				NnManager.pushsingle(rspMsg.build(), ctx);
				return;

			}

			// 2.更新用户抢庄缓存

			NnBean.UserInfo.Builder user = getCurUser(reqMsg.getUserId(), reqMsg.getRoomNo());
			user.setIsGrabLandlord(NnYesNoEnum.YES.getCode());
			user.setScoreType(reqMsg.getScoreType());

			RedisUtil.hset(NnConstans.NN_ROOM_USER_INFO_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "", JsonFormat.printToString(user.build()));

			// 3.增加用户抢庄操作
			RedisUtil.hset(NnConstans.NN_ROOM_LANDLORD_USER_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "", reqMsg.getIsGrabLandLord() + "," + reqMsg.getScoreType() + "," + System.currentTimeMillis() + "");// 设置该用户是已抢庄

			batchSendLandlord(reqMsg);

			Set<String> userSet = getAllMatchUserSet(reqMsg.getRoomNo());
			long grabLandLordSize = RedisUtil.hlen(NnConstans.NN_ROOM_LANDLORD_USER_PRE + reqMsg.getRoomNo());

			if (grabLandLordSize >= userSet.size()) {// 如果所有人都做了操作，则设置庄家
				ServerManager.executor.execute(new NnWork(reqMsg, NnWrokEnum.SEND_LAST_CARD_RESULT.getCode(), ctx));
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
			rspMsg.setCode(NnRspCodeEnum.$9999.getCode());
			rspMsg.setMsg(NnRspCodeEnum.$9999.getMsg());
			NnManager.pushsingle(rspMsg.build(), ctx);
		}
	}

	/**
	 * 抢庄推送
	 * 
	 * @param reqMsg
	 */
	public static void batchSendLandlord(NnBean.ReqMsg reqMsg) {

		String roomNo = reqMsg.getRoomNo();
		Set<String> userSet = getAllUserSet(roomNo);

		String curStatus = RedisUtil.hget(NnConstans.NN_ROOM_CUR_STATUS_PRE, roomNo);

		NnBean.UserInfo.Builder userInfo = getCurUser(reqMsg.getUserId(), roomNo);
		userInfo.clearToken();
		userInfo.clearRoomNo();
		userInfo.clearIsWin();
		userInfo.clearIsreday();
		userInfo.clearTotalGold();
		userInfo.clearCard();

		Map<String, NnBean.RspMsg> map = new HashMap<String, NnBean.RspMsg>();

		NnBean.RspMsg.Builder rspMsg = NnBean.RspMsg.newBuilder();

		NnBean.RspData.Builder rspData = NnBean.RspData.newBuilder();

		rspData.setRestTime(getRestTime(reqMsg.getRoomNo()));

		rspData.setUser(userInfo);
		rspData.setCurRoomStatus(Integer.parseInt(curStatus));
		rspMsg.setOperateType(NnPushMsgTypeEnum.GRAB_LANDLORD_PUSH.getCode());
		rspMsg.setData(rspData);
		rspMsg.setCode(NnRspCodeEnum.$0000.getCode());
		rspMsg.setMsg(NnRspCodeEnum.$0000.getMsg());

		for (String key : userSet) {

			map.put(RedisUtil.hget(NnConstans.NN_USER_CHANNEL_PRE, key), rspMsg.build());

		}
		batchSendMsg(map);

	}

	/**
	 * 设置庄家
	 * 
	 * @param reqMsg
	 * @param ctx
	 */
	public static void setLandlord(NnBean.ReqMsg reqMsg) {
		String curStaus = RedisUtil.hget(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo());

		if (Integer.parseInt(curStaus) != NnRoomMatchStatusEnum.GRAB_LANDLORD_STATUS.getCode()) {
			logger.info("当前状态不允许抢庄。");
			return;
		}

		if (RedisUtil.hexists(NnConstans.NN_ROOM_LOCK_REQ, reqMsg.getRoomNo())) {
			logger.info("拒绝操作...");
			return;
		}
		try {
			RedisUtil.hset(NnConstans.NN_ROOM_LOCK_REQ, reqMsg.getRoomNo(), System.currentTimeMillis() + "");
			{
				// 闲家加倍倒计时
				GameTimoutVo timeout=new GameTimoutVo();
				timeout.setRestTime(System.currentTimeMillis());
				timeout.setRestTimeType(NnTimeTaskEnum.FARMER_DOUBLEX.getCode());
				RedisUtil.hset(NnConstans.NN_REST_TIME_PRE, reqMsg.getRoomNo(), JSONObject.toJSONString(timeout));
				ServerManager.executorTask.schedule(new MyTimerTask(reqMsg, NnTimeTaskEnum.FARMER_DOUBLEX.getCode(),timeout.getRestTime()), NnConstans.FARMER_TIME, TimeUnit.SECONDS);
			}
			

			// 1.选取庄家[根据抢庄分数和时间判断谁是庄家]
			// 所有玩家抢庄结果数据
			Map<String, String> roomAllUserGlMap = RedisUtil.hgetall(NnConstans.NN_ROOM_LANDLORD_USER_PRE + reqMsg.getRoomNo());// 设置该用户是已抢庄
			Long landlordUserId = 0l;

			List<String> oneList = new ArrayList<String>();
			List<String> twoList = new ArrayList<String>();

			int preScoreType = 0;
			for (String key : roomAllUserGlMap.keySet()) {
				String m = roomAllUserGlMap.get(key);
				String[] n = m.split(",");
				if (n[0].equals(NnYesNoEnum.YES.getCode() + "")) {
					int scoreType = Integer.parseInt(m.split(",")[1]);
					if (scoreType >= preScoreType) {
						if (scoreType > preScoreType) {
							oneList.clear();
							oneList.add(key);
						} else {
							oneList.add(key);

						}
						preScoreType = scoreType;
					}

				} else {
					twoList.add(key);

				}

			}
			if (oneList.size() > 0) {
				landlordUserId = Long.parseLong(oneList.get((int) (Math.random() * (oneList.size()))));
			} else {
				landlordUserId = Long.parseLong(twoList.get((int) (Math.random() * (twoList.size()))));
			}

			Set<String> userSet = getAllMatchUserSet(reqMsg.getRoomNo());

			for (String key : userSet) {
				NnBean.UserInfo.Builder user = getCurUser(Long.parseLong(key), reqMsg.getRoomNo());
				if (String.valueOf(landlordUserId).equals(key)) {
					user.setPlayerType(NnUserRoleEnum.LANDLORD.getCode());// 设置庄
				} else {
					user.setPlayerType(NnUserRoleEnum.FARMER.getCode());// 设置闲家
				}

				// 设置缓存
				RedisUtil.hset(NnConstans.NN_ROOM_USER_INFO_PRE + reqMsg.getRoomNo(), key, JsonFormat.printToString(user.build()));// 更新缓存
			}
			// 当前状态为闲家选分状态
			RedisUtil.hset(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo(), NnRoomMatchStatusEnum.FARMER_STATUS.getCode() + "");
			
			

			reqMsg = NnBean.ReqMsg.newBuilder(reqMsg).setUserId(landlordUserId).setRoomNo(reqMsg.getRoomNo()).build();

			batchSendSetLanlord(reqMsg);


		} finally {
			RedisUtil.hdel(NnConstans.NN_ROOM_LOCK_REQ, reqMsg.getRoomNo());
		}

	}

	/**
	 * 推送庄家
	 * 
	 * @param reqMsg
	 */
	private static void batchSendSetLanlord(NnBean.ReqMsg reqMsg) {
		String roomNo = reqMsg.getRoomNo();
		Set<String> allUserSet = getAllUserSet(roomNo);
		Set<String> allMatchUserSet = getAllMatchUserSet(roomNo);

		String curStatus = RedisUtil.hget(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo());

		Map<String, NnBean.RspMsg> map = new HashMap<String, NnBean.RspMsg>();

		NnBean.UserInfo.Builder userInfo = getCurUser(reqMsg.getUserId(), roomNo);

		List<NnBean.UserInfo> listOtherUser = new ArrayList<NnBean.UserInfo>();
		for (String matchKey : allMatchUserSet) {
			NnBean.UserInfo.Builder otherUser = getCurUser(Long.parseLong(matchKey), roomNo);
			if (otherUser.getIsGrabLandlord() == NnYesNoEnum.YES.getCode()) {
				otherUser.clearUserId();
				otherUser.clearToken();
				otherUser.clearRoomNo();
				otherUser.clearBaseScore();
				otherUser.clearIsRoomOwn();
				otherUser.clearCard();
				otherUser.clearIsreday();
				otherUser.clearPlayerType();
				otherUser.clearIsGrabLandlord();

				listOtherUser.add(otherUser.build());
			}
		}
		for (String key : allUserSet) {

			NnBean.RspMsg.Builder rspMsg = NnBean.RspMsg.newBuilder();

			NnBean.RspData.Builder rspData = NnBean.RspData.newBuilder();

			rspData.setRestTime(getRestTime(reqMsg.getRoomNo()));

			rspData.setUser(userInfo);
			rspData.setCurRoomStatus(Integer.parseInt(curStatus));
			rspData.addAllOtherUser(listOtherUser);
			rspMsg.setOperateType(NnPushMsgTypeEnum.SET_LANDLORD_PUSH.getCode());
			rspMsg.setData(rspData);
			rspMsg.setCode(NnRspCodeEnum.$0000.getCode());
			rspMsg.setMsg(NnRspCodeEnum.$0000.getMsg());

			map.put(RedisUtil.hget(NnConstans.NN_USER_CHANNEL_PRE, key), rspMsg.build());

		}
		batchSendMsg(map);
	}

	/**
	 * 闲家设置底分
	 * 
	 * @param reqMsg
	 * @param ctx
	 */
	public static void setFarmerDouble(NnBean.ReqMsg reqMsg, ChannelHandlerContext ctx) {

		NnBean.RspMsg.Builder rspMsg = NnBean.RspMsg.newBuilder();

		// 禁止没有参加比赛的用户
		if (!RedisUtil.sismember(NnConstans.NN_ROOM_ALL_MATCH_USER_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "")) {
			rspMsg.setOperateType(NnRspMsgTypeEnum.SET_FARMER_SCORE_FEEDBACK.getCode());
			rspMsg.setCode(NnRspCodeEnum.$0003.getCode());
			rspMsg.setMsg(NnRspCodeEnum.$0003.getMsg());
			pushsingle(rspMsg.build(), ctx);
			return;
		}

		// 判断此时状态是否可以设置闲家加倍

		String curStaus = RedisUtil.hget(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo());

		if (Integer.parseInt(curStaus) != NnRoomMatchStatusEnum.FARMER_STATUS.getCode()) {
			rspMsg.setOperateType(NnRspMsgTypeEnum.SET_FARMER_SCORE_FEEDBACK.getCode());
			rspMsg.setCode(NnRspCodeEnum.$0003.getCode());
			rspMsg.setMsg(NnRspCodeEnum.$0003.getMsg());
			pushsingle(rspMsg.build(), ctx);
			return;

		}
		NnBean.UserInfo.Builder user = getCurUser(reqMsg.getUserId(), reqMsg.getRoomNo());
		// 庄家不能设置闲家操作

		if (user.getPlayerType() == NnUserRoleEnum.LANDLORD.getCode()) {
			rspMsg.setOperateType(NnRspMsgTypeEnum.SET_FARMER_SCORE_FEEDBACK.getCode());
			rspMsg.setCode(NnRspCodeEnum.$0003.getCode());
			rspMsg.setMsg(NnRspCodeEnum.$0003.getMsg());
			pushsingle(rspMsg.build(), ctx);
			return;
		}

		// 判断闲家是否设置底分

		if (RedisUtil.hexists(NnConstans.NN_ROOM_FARMER_USER_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "")) {
			rspMsg.setOperateType(NnRspMsgTypeEnum.SET_FARMER_SCORE_FEEDBACK.getCode());
			rspMsg.setCode(NnRspCodeEnum.$1114.getCode());
			rspMsg.setMsg(NnRspCodeEnum.$1114.getMsg());
			pushsingle(rspMsg.build(), ctx);
			return;

		}

		// 检查闲家设置积分是否正确
		if (!NnUtil.checkFarmerScoreIsTrue(reqMsg.getScoreType())) {
			rspMsg.setOperateType(NnRspMsgTypeEnum.GRAB_LANDLORD_FEEDBACK.getCode());
			rspMsg.setCode(NnRspCodeEnum.$0002.getCode());
			rspMsg.setMsg(NnRspCodeEnum.$0002.getMsg());
			pushsingle(rspMsg.build(), ctx);
			return;
		}

		user.setScoreType(reqMsg.getScoreType());
		user.setIsGrabFarmer(NnYesNoEnum.YES.getCode());

		RedisUtil.hset(NnConstans.NN_ROOM_USER_INFO_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "", JsonFormat.printToString(user.build()));
		RedisUtil.hset(NnConstans.NN_ROOM_FARMER_USER_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "", reqMsg.getScoreType() + "");

		batchSendSetFarmer(reqMsg);

		sendLastCard(reqMsg);

	}

	/**
	 * 设置闲家推送
	 * 
	 * @param reqMsg
	 */
	public static void batchSendSetFarmer(NnBean.ReqMsg reqMsg) {

		String roomNo = reqMsg.getRoomNo();
		Set<String> userSet = getAllUserSet(roomNo);

		String curStatus = RedisUtil.hget(NnConstans.NN_ROOM_CUR_STATUS_PRE, roomNo);

		NnBean.UserInfo.Builder userInfo = getCurUser(reqMsg.getUserId(), roomNo);
		userInfo.clearToken();
		userInfo.clearRoomNo();
		userInfo.clearIsWin();
		userInfo.clearIsreday();
		userInfo.clearIsGrabLandlord();
		userInfo.clearTotalGold();
		userInfo.clearCard();

		Map<String, NnBean.RspMsg> map = new HashMap<String, NnBean.RspMsg>();

		NnBean.RspMsg.Builder rspMsg = NnBean.RspMsg.newBuilder();

		NnBean.RspData.Builder rspData = NnBean.RspData.newBuilder();

		rspData.setRestTime(getRestTime(reqMsg.getRoomNo()));

		rspData.setUser(userInfo);
		rspData.setCurRoomStatus(Integer.parseInt(curStatus));
		rspMsg.setOperateType(NnPushMsgTypeEnum.FARMER_SET_SCORE_DOUBLE_PUSH.getCode());
		rspMsg.setData(rspData);
		rspMsg.setCode(NnRspCodeEnum.$0000.getCode());
		rspMsg.setMsg(NnRspCodeEnum.$0000.getMsg());

		for (String key : userSet) {
			map.put(RedisUtil.hget(NnConstans.NN_USER_CHANNEL_PRE, key), rspMsg.build());

		}
		batchSendMsg(map);
	}

	/**
	 * 发送最后一张牌
	 * 
	 * @param reqMsg
	 */
	public static void sendLastCard(NnBean.ReqMsg reqMsg) {

		String curStaus = RedisUtil.hget(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo());

		if (Integer.parseInt(curStaus) != NnRoomMatchStatusEnum.FARMER_STATUS.getCode()) {
			return;

		}

		Set<String> set = getAllMatchUserSet(reqMsg.getRoomNo());

		boolean isSend = true;
		for (String key : set) {
			NnBean.UserInfo.Builder user = getCurUser(Long.parseLong(key), reqMsg.getRoomNo());
			if (user.getPlayerType() == NnUserRoleEnum.FARMER.getCode() && user.getIsGrabFarmer() == 0) {
				isSend = false;
				break;
			}
		}

		if (isSend) {

			if (RedisUtil.hexists(NnConstans.NN_ROOM_LOCK_REQ, reqMsg.getRoomNo())) {
				return;
			}
			try {
				RedisUtil.hset(NnConstans.NN_ROOM_LOCK_REQ, reqMsg.getRoomNo(), System.currentTimeMillis() + "");
				// 明牌倒计时设置
				GameTimoutVo timeout=new GameTimoutVo();
				timeout.setRestTime(System.currentTimeMillis());
				timeout.setRestTimeType(NnTimeTaskEnum.SHOW_CARD_RESULT.getCode());
				RedisUtil.hset(NnConstans.NN_REST_TIME_PRE, reqMsg.getRoomNo(), JSONObject.toJSONString(timeout));
				// 增加明牌倒计时
				ServerManager.executorTask.schedule(new MyTimerTask(reqMsg, NnTimeTaskEnum.SHOW_CARD_RESULT.getCode(),timeout.getRestTime()), NnConstans.SHOW_MATCH_TIME, TimeUnit.SECONDS);
				

				// 发送最后一张牌
				String str = RedisUtil.hget(NnConstans.NN_ROOM_USER_ALL_CARD_PRE, reqMsg.getRoomNo());

				Map<String, List<Integer>> userCardMap = (Map<String, List<Integer>>) JSONObject.parse(str);

				for (String key : set) {

					NnBean.UserInfo.Builder user = getCurUser(Long.parseLong(key), reqMsg.getRoomNo());
					if (user.getPlayerType() != NnUserRoleEnum.GUEST.getCode()) {
						NnBean.CardInfo.Builder cardInfo = NnBean.CardInfo.newBuilder();
						List<Integer> cardList = userCardMap.get(key + "");
						cardInfo.addAllNum(cardList);
						user.setCard(cardInfo);
						user.setCardCount(cardList.size());
						RedisUtil.hset(NnConstans.NN_ROOM_USER_INFO_PRE + reqMsg.getRoomNo(), key, JsonFormat.printToString(user.build()));
					}
				}
				RedisUtil.hset(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo(), NnRoomMatchStatusEnum.PLAY_GAME_STATUS.getCode() + "");

			
				batchSendLastCard(reqMsg);

			

			} finally {
				RedisUtil.hdel(NnConstans.NN_ROOM_LOCK_REQ, reqMsg.getRoomNo());
			}

		}

	}

	/**
	 * 用户发牌批量推送
	 * 
	 * @param reqMsg
	 */
	private static void batchSendLastCard(ReqMsg reqMsg) {

		String roomNo = reqMsg.getRoomNo();
		Set<String> allUserSet = getAllUserSet(roomNo);
		Set<String> allMatchUserSet = getAllMatchUserSet(roomNo);

		String curStatus = RedisUtil.hget(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo());

		Map<String, NnBean.RspMsg> map = new HashMap<String, NnBean.RspMsg>();

		for (String key : allUserSet) {

			NnBean.UserInfo.Builder userInfo = getCurUser(Long.parseLong(key), roomNo);

			NnBean.RspMsg.Builder rspMsg = NnBean.RspMsg.newBuilder();

			NnBean.RspData.Builder rspData = NnBean.RspData.newBuilder();

			rspData.setRestTime(getRestTime(reqMsg.getRoomNo()));

			rspData.setUser(userInfo);
			rspData.setCurRoomStatus(Integer.parseInt(curStatus));

			List<NnBean.UserInfo> listOtherUser = new ArrayList<NnBean.UserInfo>();
			for (String matchKey : allMatchUserSet) {

				if (!key.equals(matchKey)) {
					NnBean.UserInfo.Builder otherUser = getCurUser(Long.parseLong(matchKey), roomNo);
					otherUser.clearToken();
					otherUser.clearRoomNo();
					otherUser.clearIsWin();
					otherUser.clearIsreday();
					otherUser.clearIsGrabLandlord();
					otherUser.clearScoreType();
					otherUser.clearTotalGold();
					otherUser.clearCard();
					listOtherUser.add(otherUser.build());
				}
			}
			rspData.addAllOtherUser(listOtherUser);
			rspMsg.setOperateType(NnPushMsgTypeEnum.SEND_LAST_CARD_PUSH.getCode());
			rspMsg.setData(rspData);
			rspMsg.setCode(NnRspCodeEnum.$0000.getCode());
			rspMsg.setMsg(NnRspCodeEnum.$0000.getMsg());

			map.put(RedisUtil.hget(NnConstans.NN_USER_CHANNEL_PRE, key), rspMsg.build());

		}
		batchSendMsg(map);
	}

	/**
	 * 展示比赛结果
	 * 
	 * @param reqMsg
	 * @param ctx
	 * @throws InterruptedException
	 */
	public static void setIsShowCard(NnBean.ReqMsg reqMsg, ChannelHandlerContext ctx) {
		NnBean.RspMsg.Builder rspMsg = NnBean.RspMsg.newBuilder();

		// 禁止没有准备的游客进行明牌
		if (!RedisUtil.sismember(NnConstans.NN_ROOM_ALL_MATCH_USER_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "")) {
			rspMsg.setOperateType(NnRspMsgTypeEnum.SHOW_CARD_FEEDBACK.getCode());
			rspMsg.setCode(NnRspCodeEnum.$0003.getCode());
			rspMsg.setMsg(NnRspCodeEnum.$0003.getMsg());
			NnManager.pushsingle(rspMsg.build(), ctx);
			return;
		}

		// 判断此时状态是否可以名牌

		String curStaus = RedisUtil.hget(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo());

		if (Integer.parseInt(curStaus) != NnRoomMatchStatusEnum.PLAY_GAME_STATUS.getCode()) {
			rspMsg.setOperateType(NnRspMsgTypeEnum.SHOW_CARD_FEEDBACK.getCode());
			rspMsg.setCode(NnRspCodeEnum.$0003.getCode());
			rspMsg.setMsg(NnRspCodeEnum.$0003.getMsg());
			logger.info(NnRspCodeEnum.$0003.getMsg());
			NnManager.pushsingle(rspMsg.build(), ctx);
			return;

		}

		NnBean.UserInfo.Builder uInfo = getCurUser(reqMsg.getUserId(), reqMsg.getRoomNo());
		if (uInfo.getIsShowCard() == NnYesNoEnum.YES.getCode()) {
			rspMsg.setCode(NnRspCodeEnum.$1110.getCode());
			rspMsg.setMsg(NnRspCodeEnum.$1110.getMsg());
			logger.info(NnRspCodeEnum.$1110.getMsg());
			NnManager.pushsingle(rspMsg.build(), ctx);
			return;
		}

		uInfo.setIsShowCard(NnYesNoEnum.YES.getCode());
		RedisUtil.hset(NnConstans.NN_ROOM_USER_INFO_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "", JsonFormat.printToString(uInfo.build()));

		// rspMsg.setOperateType(NnRspMsgTypeEnum.SHOW_CARD_FEEDBACK.getCode());
		// rspMsg.setCode(NnRspCodeEnum.$0000.getCode());
		// rspMsg.setMsg(NnRspCodeEnum.$0000.getMsg());
		//
		// pushsingle(rspMsg.build(), ctx);

		batchSendShowCard(reqMsg);

		// 判断所有人是否点击名牌按钮
		Set<String> userSet = getAllMatchUserSet(reqMsg.getRoomNo());
		boolean flag = true;
		for (String key : userSet) {
			NnBean.UserInfo.Builder user = getCurUser(Long.parseLong(key), reqMsg.getRoomNo());
			if (user.getIsShowCard() == NnYesNoEnum.NO.getCode()) {
				flag = false;
			}
		}
		if (flag) {
			ServerManager.executor.execute(new NnWork(reqMsg, NnWrokEnum.SHOW_MATCH_RESULT.getCode(), ctx));
		}

	}

	/**
	 * 明牌推送
	 * 
	 * @param reqMsg
	 */
	public static void batchSendShowCard(NnBean.ReqMsg reqMsg) {

		String roomNo = reqMsg.getRoomNo();
		Set<String> allUserSet = getAllUserSet(roomNo);

		String curStatus = RedisUtil.hget(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo());

		Map<String, NnBean.RspMsg> map = new HashMap<String, NnBean.RspMsg>();

		NnBean.UserInfo.Builder userInfo = getCurUser(reqMsg.getUserId(), roomNo);
		for (String key : allUserSet) {

			NnBean.RspMsg.Builder rspMsg = NnBean.RspMsg.newBuilder();

			NnBean.RspData.Builder rspData = NnBean.RspData.newBuilder();

			rspData.setRestTime(getRestTime(reqMsg.getRoomNo()));

			rspData.setUser(userInfo);
			rspData.setCurRoomStatus(Integer.parseInt(curStatus));

			rspMsg.setOperateType(NnPushMsgTypeEnum.SET_SHOW_CARD_PUSH.getCode());
			rspMsg.setData(rspData);
			rspMsg.setCode(NnRspCodeEnum.$0000.getCode());
			rspMsg.setMsg(NnRspCodeEnum.$0000.getMsg());

			map.put(RedisUtil.hget(NnConstans.NN_USER_CHANNEL_PRE, key), rspMsg.build());

		}
		batchSendMsg(map);
	}

	/**
	 * 展示比赛结果
	 * 
	 * @param reqMsg
	 * @param ctx
	 */
	public static void showMatchResult(NnBean.ReqMsg reqMsg) {
		logger.info("计算比赛结果开始....");
		long stime = System.currentTimeMillis();
		// 只有当前状态在名牌阶段才能调用此接口
		String curStaus = RedisUtil.hget(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo());
		if (Integer.parseInt(curStaus) != NnRoomMatchStatusEnum.PLAY_GAME_STATUS.getCode()) {
			logger.info("当前状态无法展示比赛结果");
			return;
		}
		if (curStaus.equals(NnRoomMatchStatusEnum.SHOW_MATCH_RESULT_STATUS.getCode() + "")) {
			logger.info("正在计算比赛结果，请勿重复提交");
			return;
		}
		if (RedisUtil.hexists(NnConstans.NN_ROOM_LOCK_REQ, reqMsg.getRoomNo())) {
			logger.info("请勿重复提交");
			return;
		}

		// 加个用户操作缓存锁
		if (RedisUtil.hexists(NnConstans.NN_USER_LOCK_REQ, reqMsg.getUserId() + "")) {
			logger.info("请勿重复提交");
			return;
		}
		RedisUtil.hset(NnConstans.NN_USER_LOCK_REQ, reqMsg.getUserId() + "", System.currentTimeMillis() + "");
		try {
			RedisUtil.hset(NnConstans.NN_ROOM_LOCK_REQ, reqMsg.getRoomNo(), System.currentTimeMillis() + "");
		
			{
				// 重置展示比赛结果倒计时
				GameTimoutVo timeout = new GameTimoutVo();
				timeout.setRestTime(System.currentTimeMillis());
				timeout.setRestTimeType(NnTimeTaskEnum.UNDEINDED.getCode());
				RedisUtil.hset(NnConstans.NN_REST_TIME_PRE, reqMsg.getRoomNo(), JSONObject.toJSONString(timeout));
				
			}
			
			
			// 比赛结果判断
			Set<String> userSet = getAllMatchUserSet(reqMsg.getRoomNo());
			// 庄家用户
			NnBean.UserInfo.Builder landlordUser = NnBean.UserInfo.newBuilder();
			long landlordId = 0;
			for (String key : userSet) {
				landlordUser = getCurUser(Long.parseLong(key), reqMsg.getRoomNo());
				if (NnUserRoleEnum.LANDLORD.getCode() == landlordUser.getPlayerType()) {
					landlordId = landlordUser.getUserId();
					break;
				}
			}
			if (landlordId == 0) {
				// 设置用户状态准备为未准备
				for (String key : userSet) {
					NnBean.UserInfo.Builder u = getCurUser(Long.parseLong(key), reqMsg.getRoomNo());
					u.setIsreday(NnYesNoEnum.NO.getCode());
					RedisUtil.hset(NnConstans.NN_ROOM_USER_INFO_PRE + reqMsg.getRoomNo(), key, JsonFormat.printToString(u.build()));
				}
				return;
			}
			// 获取房间的信息
			NnBean.RoomInfo.Builder roomInfo = getRoomInfo(reqMsg.getRoomNo());

			// 庄家card
			List<Integer> landlordNumList = landlordUser.getCard().getNumList();

			List<NnBean.UserInfo.Builder> listUser = new ArrayList<NnBean.UserInfo.Builder>();

			int landlordDoubleType = NnCardUtil.getCardType(landlordNumList);
			int landlordDouble = NnUtil.getCardTypeDouble(landlordDoubleType);

			for (NnBean.CardTypeDouble doubleType : roomInfo.getCardDoubleList()) {

				if (doubleType.getCardType().getNumber() == landlordDoubleType) {
					landlordDouble = NnUtil.getCardTypeSpecialDouble(doubleType.getCardType().getNumber());
				}
			}
			for (String key : userSet) {
				NnBean.UserInfo.Builder uInfo = getCurUser(Long.parseLong(key), reqMsg.getRoomNo());
				if (uInfo.getUserId() != landlordId) {
					// 闲家card
					List<Integer> farmerNumList = uInfo.getCard().getNumList();

					boolean matchResult = NnCardUtil.compareCard(landlordNumList, farmerNumList);
					int farmerDoubleType = NnCardUtil.getCardType(farmerNumList);

					// 积分计算[(庄家底分+闲家底分)*牛的倍数]
					// 底分
					int baseScore = landlordUser.getBaseScore() * landlordUser.getScoreType() + uInfo.getBaseScore() * uInfo.getScoreType();

					int farmertDouble = NnUtil.getCardTypeDouble(farmerDoubleType);

					for (NnBean.CardTypeDouble doubleType : roomInfo.getCardDoubleList()) {

						if (doubleType.getCardType().getNumber() == farmerDoubleType) {
							farmertDouble = NnUtil.getCardTypeSpecialDouble(doubleType.getCardType().getNumber());
						}
					}

					// 庄家赢
					if (matchResult) {

						long totalGold = NnManagerDao.instance().getUserGold(uInfo.getUserId());
						uInfo.setUserGold((int) totalGold);
						uInfo.setTotalGold((int)totalGold);
						int winGold = baseScore * landlordDouble;
						if (uInfo.getUserGold()< winGold) {// 如果闲家金币不够支付,则直接支付当前全部金币
							winGold = (int) uInfo.getUserGold();
						}

						landlordUser.setCurMatchGold(landlordUser.getCurMatchGold() + winGold);

						uInfo.setCurMatchGold(-winGold);

					} else {
						landlordUser.setCurMatchGold(landlordUser.getCurMatchGold() - baseScore * farmertDouble);

						uInfo.setCurMatchGold(baseScore * farmertDouble);
						uInfo.setIsWin(NnYesNoEnum.YES.getCode());
					}

					List<Integer> listSort = NnCardUtil.sortCards(new ArrayList<Integer>(farmerNumList));
					int cardType = NnCardUtil.getCardType(uInfo.getCard().getNumList());
					NnBean.CardInfo.Builder cardInfo = NnBean.CardInfo.newBuilder(uInfo.getCard());
					cardInfo.clearNum().addAllNum(listSort);
					cardInfo.setCardType(cardType);
					uInfo.setCard(cardInfo);

					listUser.add(uInfo);
				}

			}
			List<Integer> listSort = NnCardUtil.sortCards(new ArrayList<Integer>(landlordUser.getCard().getNumList()));
			int cardType = NnCardUtil.getCardType(landlordUser.getCard().getNumList());
			NnBean.CardInfo.Builder cardInfo = NnBean.CardInfo.newBuilder(landlordUser.getCard());
			cardInfo.clearNum().addAllNum(listSort);
			cardInfo.setCardType(cardType);
			landlordUser.setCard(cardInfo);

			if (landlordUser.getCurMatchGold() >= 0) {
				landlordUser.setIsWin(NnYesNoEnum.YES.getCode());
			}

			long totalGold = NnManagerDao.instance().getUserGold(landlordUser.getUserId());
			landlordUser.setUserGold((int) totalGold);
			landlordUser.setTotalGold((int) totalGold);
			if (landlordUser.getUserGold() + landlordUser.getCurMatchGold() < 0) {
				// 计算闲家赢家用户的总金币数
				int userWinTotalGold = 0;
				for (NnBean.UserInfo.Builder user : listUser) {

					if (user.getCurMatchGold() >= 0) {
						userWinTotalGold = userWinTotalGold + user.getCurMatchGold();
					}
				}

				int userRealWinTotalGold = 0;

				for (NnBean.UserInfo.Builder user : listUser) {

					if (user.getCurMatchGold() >= 0) {

						int m = (int) Math.floor((user.getCurMatchGold() / userWinTotalGold) * landlordUser.getTotalGold());
						userRealWinTotalGold = userRealWinTotalGold + m;
						user.setCurMatchGold(m);
						user.setTotalGold(user.getTotalGold() + m);
					} else {
						user.setTotalGold(user.getCurMatchGold() + user.getTotalGold());
					}

				}

				landlordUser.setCurMatchGold(-userRealWinTotalGold);

				landlordUser.setTotalGold((int) (landlordUser.getTotalGold() - userRealWinTotalGold));

			}

			List<NnBean.UserInfo> rankList = new ArrayList<NnBean.UserInfo>();

			for (NnBean.UserInfo.Builder user : listUser) {
				rankList.add(user.build());
				// 更新闲家缓存
				RedisUtil.hset(NnConstans.NN_ROOM_USER_INFO_PRE + reqMsg.getRoomNo(), user.getUserId() + "", JsonFormat.printToString(user.build()));

			}

			// 更新庄家缓存
			RedisUtil.hset(NnConstans.NN_ROOM_USER_INFO_PRE + reqMsg.getRoomNo(), landlordUser.getUserId() + "", JsonFormat.printToString(landlordUser.build()));
			rankList.add(landlordUser.build());
			{
				// 更新排行榜
				Collections.sort(rankList, new Comparator<NnBean.UserInfo>() {
					public int compare(NnBean.UserInfo org, NnBean.UserInfo dest) {
						return Integer.valueOf(org.getCurMatchScore()).compareTo(Integer.valueOf(dest.getCurMatchScore()));
					}
				});

				List<NnBean.UserInfo> listRankUser = new ArrayList<NnBean.UserInfo>();
				NnBean.resultRank.Builder rankInfo = NnBean.resultRank.newBuilder();

				for (int i = 0; i < rankList.size(); i++) {
					NnBean.UserInfo u = rankList.get(i);
					NnBean.UserInfo.Builder user = NnBean.UserInfo.newBuilder(u);
					user.setCurRank(i + 1);
					RedisUtil.hset(NnConstans.NN_ROOM_USER_INFO_PRE + reqMsg.getRoomNo(), user.getUserId() + "", JsonFormat.printToString(user.build()));

					NnBean.UserInfo.Builder newUser = NnBean.UserInfo.newBuilder();
					newUser.setNickName(user.getNickName());
					newUser.setGender(user.getGender());
					newUser.setCurRank(user.getCurRank());
					newUser.setScoreType(user.getScoreType());
					newUser.setPlayerType(user.getPlayerType());
					newUser.setIsWin(user.getIsWin());
					newUser.setUserId(user.getUserId());
					if (user.getCurMatchGold() >= 0) {
						newUser.setIsWin(1);
					} else {
						newUser.setIsWin(0);
					}
					newUser.setPosition(user.getPosition());
					newUser.setCurMatchGold(user.getCurMatchGold());
					NnBean.CardInfo.Builder card = user.getCard().toBuilder();
					card.setCardType(NnCardTypeTransEnum.getDestType(card.getCardType()));
					card.clearNum();
					newUser.setCard(card);
					newUser.addAllCardType(user.getCardTypeList());
					listRankUser.add(newUser.build());

				}
				rankInfo.addAllUserRank(listRankUser);
				RedisUtil.hset(NnConstans.NN_ROOM_USER_RNK, reqMsg.getRoomNo() + "", JsonFormat.printToString(rankInfo.build()));
			}

			userSet = getAllMatchUserSet(reqMsg.getRoomNo());

			NnRoom room = getRoomVo(reqMsg.getRoomNo());

			// 封装缓存数据

			final List<DbVo> listDb = new ArrayList<DbVo>();

			for (NnBean.UserInfo user : rankList) {
				DbVo dbVo = new DbVo();

				dbVo.setUserId(user.getUserId());
				dbVo.setWinGold(user.getCurMatchGold());

				String orderNo = UUID.randomUUID().toString();
				GoldRecord gr = new GoldRecord();
				gr.setUserId(user.getUserId());
				gr.setOrderNo(orderNo);
				gr.setGoldCount(room.getCostGold().intValue());
				gr.setCostType(GoldTypeEnum.NIU_NIU_COST.getType());
				gr.setBindId(room.getCreateUserId());
				gr.setCreateDate(new Timestamp(System.currentTimeMillis()));

				dbVo.setGoldRecord(gr);

				UserCostRecord ucr = new UserCostRecord();
				ucr.setUserId(user.getUserId());
				ucr.setRoomNo(room.getRoomNo());
				ucr.setRoomId(room.getId());
				ucr.setOrderNo(orderNo);
				ucr.setGameType(GoldTypeEnum.NIU_NIU_COST.getType());
				ucr.setCostGold(room.getCostGold().intValue());
				ucr.setIsDel(NnYesNoEnum.NO.getCode());
				ucr.setBindId(room.getId());
				ucr.setCreateDate(new Date());

				dbVo.setUserCostRecord(ucr);

				NnRoomMatchUser nrmu = new NnRoomMatchUser();
				nrmu.setUserId(user.getUserId());
				nrmu.setRoomId(room.getId());
				nrmu.setRoomNo(room.getRoomNo());
				nrmu.setMatchNum(roomInfo.getRoomCurMatchCount());
				nrmu.setTotalGold((long) user.getTotalGold());
				nrmu.setBaseGold(user.getBaseScore());
				nrmu.setCostGold(room.getCostGold().intValue());
				nrmu.setPlayerRole(user.getPlayerType());
				nrmu.setWinGold(user.getCurMatchGold());
				nrmu.setDoublex(user.getScoreType());
				if (user.getCurMatchGold() >= 0) {
					nrmu.setIsWin(NnYesNoEnum.YES.getCode());
				} else {
					nrmu.setIsWin(NnYesNoEnum.NO.getCode());
				}

				String cards = "";
				for (int num : user.getCard().getNumList()) {
					cards += num + ",";

				}
				nrmu.setCards(cards.substring(0, cards.length()));
				nrmu.setCardType(NnCardUtil.getCardType(user.getCard().getNumList()));
				nrmu.setOrderNo(orderNo);
				if (RedisUtil.hexists(NnConstans.NN_BUG_USER_PRE + reqMsg.getRoomNo(), user.getUserId() + "")) {
					nrmu.setIsBug(NnYesNoEnum.YES.getCode());
				} else {
					nrmu.setIsBug(NnYesNoEnum.NO.getCode());
				}
				nrmu.setCreateDate(new Date());

				dbVo.setNnRoomMatchUser(nrmu);
				UserMatchRecord umr = new UserMatchRecord();
				umr.setUserId(user.getUserId());
				umr.setWinGold(user.getCurMatchGold());
				umr.setRoomId(room.getId());
				umr.setOrderNo(orderNo);
				umr.setGameType(GameTypeEnum.NIU_NIU.getType());
				umr.setBindId(room.getId());
				umr.setCreateDate(new Date());
				umr.setMark("");
				dbVo.setUserMatchRecord(umr);

				listDb.add(dbVo);
				//
			}
			NnManagerDao.instance().addDb(listDb);
			// 增加倒计时
			long userRedayTime = System.currentTimeMillis();
			// 设置用户状态准备为未准备
			for (String key : userSet) {
				NnBean.UserInfo.Builder u = getCurUser(Long.parseLong(key), reqMsg.getRoomNo());
				u.setIsreday(NnYesNoEnum.NO.getCode());

				long userGold = NnManagerDao.instance().getUserGold(u.getUserId());
				u.setUserGold((int) userGold);
				u.setTotalGold((int)userGold);
				
				GameTimoutVo timeVo=new GameTimoutVo();
				timeVo.setRestTime(userRedayTime);
				timeVo.setRestTimeType(NnTimeTaskEnum.USER_MATCH_END_REDAY.getCode());
				RedisUtil.hset(NnConstans.NN_REST_TIME_PRE+reqMsg.getRoomNo(), u.getUserId()+"",JSONObject.toJSONString(timeVo));
				
				u.setRedayTime(NnConstans.USER_RDAY_TIME);
//				NnBean.ReqMsg.Builder newReqMsg=NnBean.ReqMsg.newBuilder().setUserId(u.getUserId()).setRoomNo(reqMsg.getRoomNo());
				
//				ServerManager.executorTask.schedule(new MyTimerTask(newReqMsg.build(), NnTimeTaskEnum.USER_MATCH_END_REDAY.getCode(),timeVo.getRestTime()), NnConstans.USER_RDAY_TIME, TimeUnit.SECONDS);
				RedisUtil.hset(NnConstans.NN_ROOM_USER_INFO_PRE + reqMsg.getRoomNo(), key, JsonFormat.printToString(u.build()));

			}
//			{
//				// 重置展示比赛结果倒计时
				GameTimoutVo timeout = new GameTimoutVo();
				timeout.setRestTime(userRedayTime);
				timeout.setRestTimeType(NnTimeTaskEnum.USER_MATCH_END_REDAY.getCode());
				RedisUtil.hset(NnConstans.NN_REST_TIME_PRE, reqMsg.getRoomNo(), JSONObject.toJSONString(timeout));
				ServerManager.executorTask.schedule(new MyTimerTask(reqMsg, NnTimeTaskEnum.USER_MATCH_END_REDAY.getCode(),userRedayTime), NnConstans.USER_RDAY_TIME, TimeUnit.SECONDS);
//				
//			}

			// 更新当前比赛状态为已结束
			NnBean.RoomInfo.Builder nnRoomInfo = getRoomInfo(reqMsg.getRoomNo());
			nnRoomInfo.setRoomCurMatchStatus(NumEnum.ZERO.getNumInteger());
			RedisUtil.hset(NnConstans.NN_ROOM_PRE+reqMsg.getRoomNo(),"roomInfo",JsonFormat.printToString(nnRoomInfo.build()));

		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			e1.printStackTrace();
		} finally {
			RedisUtil.hdel(NnConstans.NN_USER_LOCK_REQ, reqMsg.getUserId() + "");
			// 展示比赛结果
			RedisUtil.hset(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo(), NnRoomMatchStatusEnum.SHOW_MATCH_RESULT_STATUS.getCode() + "");
			batchSendMatchResult(reqMsg);
			sendGoldNotEnough(reqMsg.getRoomNo());
			RedisUtil.hdel(NnConstans.NN_ROOM_LOCK_REQ, reqMsg.getRoomNo());
		}
		long mtime = System.currentTimeMillis();
		logger.info("计算比赛结果总耗时:" + (mtime - stime));

	}

	/**
	 * 推送比赛结果
	 * 
	 * @param reqMsg
	 */
	private static void batchSendMatchResult(NnBean.ReqMsg reqMsg) {

		String roomNo = reqMsg.getRoomNo();
		Set<String> allUserSet = getAllUserSet(roomNo);
		Set<String> allMatchUserSet = getAllMatchUserSet(roomNo);

		String curStatus = RedisUtil.hget(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo());

		Map<String, NnBean.RspMsg> map = new HashMap<String, NnBean.RspMsg>();

		for (String key : allUserSet) {

			NnBean.UserInfo.Builder userInfo = getCurUser(Long.parseLong(key), roomNo);

			NnBean.RspMsg.Builder rspMsg = NnBean.RspMsg.newBuilder();

			NnBean.RspData.Builder rspData = NnBean.RspData.newBuilder();

			rspData.setRestTime(getRestTime(reqMsg.getRoomNo()));

			rspData.setUser(userInfo);
			rspData.setCurRoomStatus(Integer.parseInt(curStatus));

			List<NnBean.UserInfo> listOtherUser = new ArrayList<NnBean.UserInfo>();
			for (String matchKey : allMatchUserSet) {

				if (!key.equals(matchKey)) {
					NnBean.UserInfo.Builder otherUser = getCurUser(Long.parseLong(matchKey), roomNo);
					otherUser.clearToken();
					otherUser.clearRoomNo();
					otherUser.clearIsreday();
					otherUser.clearIsGrabLandlord();
					otherUser.clearScoreType();
					listOtherUser.add(otherUser.build());
				}
			}

			rspData.addAllRank(getUserRank(roomNo).getUserRankList());

			rspData.addAllOtherUser(listOtherUser);
			rspMsg.setOperateType(NnPushMsgTypeEnum.SEND_MATCH_RESULT_PUSH.getCode());
			rspMsg.setData(rspData);
			rspMsg.setCode(NnRspCodeEnum.$0000.getCode());
			rspMsg.setMsg(NnRspCodeEnum.$0000.getMsg());

			map.put(RedisUtil.hget(NnConstans.NN_USER_CHANNEL_PRE, key), rspMsg.build());

		}
		batchSendMsg(map);

	}

	/**
	 * 获取排行信息
	 * 
	 * @param roomNo
	 * @return
	 */
	private static NnBean.resultRank.Builder getUserRank(String roomNo) {
		NnBean.resultRank.Builder rankInfo = NnBean.resultRank.newBuilder();
		String rankStr = RedisUtil.hget(NnConstans.NN_ROOM_USER_RNK, roomNo);
		try {
			JsonFormat.merge(rankStr, rankInfo);
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}

		return NnBean.resultRank.newBuilder(rankInfo.build());
	}

	/**
	 * 推送金币不知玩家
	 * 
	 * @param roomNo
	 */
	private static void sendGoldNotEnough(String roomNo) {

		NnRoom nnRoom = getRoomVo(roomNo);

		Map<String, NnBean.UserInfo.Builder> noEnoughMap = new HashMap<String, NnBean.UserInfo.Builder>();

		Set<String> clubUserSet = getAllMatchUserSet(roomNo);
		for (String key : clubUserSet) {
			NnBean.UserInfo.Builder user = getCurUser(Long.parseLong(key), roomNo);
			if ((user.getUserGold() < nnRoom.getOutLimitGold())) {
				noEnoughMap.put(key, user);
			}
		}

		if (!noEnoughMap.isEmpty()) {
			Map<String, NnBean.RspMsg> map = new HashMap<String, NnBean.RspMsg>();

			for (String keya : clubUserSet) {

				NnBean.RspMsg.Builder rspMsg = NnBean.RspMsg.newBuilder();

				NnBean.RspData.Builder rspData = NnBean.RspData.newBuilder();

				for (String keyb : noEnoughMap.keySet()) {

					if (keya == keyb) {// 如果是自己金币不足
						rspData.setUser(getCurUser(Long.parseLong(keyb), roomNo));
					} else {
						rspData.addOtherUser(getCurUser(Long.parseLong(keyb), roomNo));
					}

				}
				rspMsg.setData(rspData);
				rspMsg.setOperateType(NnPushMsgTypeEnum.SEND_GOLD_NOT_ENOUGH_PUSH.getCode());
				rspMsg.setCode(NnRspCodeEnum.$0000.getCode());
				rspMsg.setMsg(NnRspCodeEnum.$0000.getMsg());
				map.put(RedisUtil.hget(NnConstans.NN_USER_CHANNEL_PRE, keya), rspMsg.build());
			}

			batchSendMsg(map);

		}
	}

	/**
	 * 退出房间
	 * 
	 * @param reqMsg
	 * @return
	 */
	public static void exitRoom(NnBean.ReqMsg reqMsg, ChannelHandlerContext ctx) {

		NnBean.RspMsg.Builder rspMsg = NnBean.RspMsg.newBuilder();

		rspMsg.setOperateType(NnRspMsgTypeEnum.EXIT_ROOM_FEEDBACK.getCode());

		NnBean.UserInfo.Builder user = getCurUser(reqMsg.getUserId(), reqMsg.getRoomNo());
		NnBean.RoomInfo.Builder roomInfo = getRoomInfo(reqMsg.getRoomNo());
		if (RedisUtil.sismember(NnConstans.NN_ROOM_ALL_MATCH_USER_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "") && roomInfo.getRoomCurMatchStatus() == 1) {
			rspMsg.setCode(NnRspCodeEnum.$1118.getCode());
			rspMsg.setMsg(NnRspCodeEnum.$1118.getMsg());
			pushsingle(rspMsg.build(), ctx);
			return;
		}

		Set<String> allUserSet = getAllUserSet(reqMsg.getRoomNo());

		if (allUserSet.size() <= 1) {

			// 直接解散房间，推送用户退出房间，删除缓存
			batchSendClose(reqMsg);
			clearUserRedis(reqMsg);
			clearRedis(reqMsg);

			return;

		} else {

			Jedis redis = null;
			try {
				redis = RedisUtil.getJedis();
				Transaction tx = redis.multi();
				NnBean.RspData.Builder rspData = NnBean.RspData.newBuilder();
				NnBean.UserInfo.Builder curUser = NnBean.UserInfo.newBuilder();
				curUser.setPosition(user.getPosition());
				rspData.setUser(curUser);

				tx.hdel(NnConstans.NN_ROOM_USER_INFO_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "");
				tx.srem(NnConstans.NN_ROOM_ALL_USER_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "");// 所有比赛用户
				tx.srem(NnConstans.NN_ROOM_ALL_READY_USER_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "");// 所有准备用户
				tx.srem(NnConstans.NN_ROOM_ALL_MATCH_USER_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "");// 所有用户

				roomInfo.setRoomCurPersonCount(roomInfo.getRoomCurPersonCount() - 1);// 设置当前人数
				roomInfo.setRoomCurStatus(NnYesNoEnum.NO.getCode());
				tx.hset(NnConstans.NN_ROOM_PRE + roomInfo.getRoomNo(), "roomInfo", JsonFormat.printToString(roomInfo.build()));
				tx.hdel(NnConstans.NN_USER_CHANNEL_PRE, reqMsg.getUserId() + "");// 删除渠道缓存
				tx.hdel(NnConstans.NN_BUG_USER_PRE + roomInfo.getRoomNo(), reqMsg.getUserId() + "");
				tx.hdel(NnConstans.NN_ROOM_USER_REDAY_TIME_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "");
				tx.hdel(NnConstans.NN_REST_TIME_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "");
				tx.exec();

				delUserPostion(reqMsg.getUserId(), reqMsg.getRoomNo());
				NnManagerDao.instance().deleteRoomUser(reqMsg.getUserId(), roomInfo.getRoomNo(), GameTypeEnum.NIU_NIU.getType());
				// 退回房卡

				rspMsg.setData(rspData);
				rspMsg.setCode(NnRspCodeEnum.$1101.getCode());
				rspMsg.setMsg(NnRspCodeEnum.$1101.getMsg());
				pushsingle(rspMsg.build(), ctx);// 推送单个

				Map<String, NnBean.RspMsg> map = new HashMap<String, NnBean.RspMsg>();

				for (String key : allUserSet) {
					rspMsg.setOperateType(NnPushMsgTypeEnum.EXIT_ROOM_PUSH.getCode());
					rspMsg.setCode(NnRspCodeEnum.$0000.getCode());
					rspMsg.setMsg(NnRspCodeEnum.$0000.getMsg());
					map.put(RedisUtil.hget(NnConstans.NN_USER_CHANNEL_PRE, key), rspMsg.build());
				}
				batchSendMsg(map);

				Set<String> allUser = getAllUserSet(reqMsg.getRoomNo());// 所有用户
				Set<String> allReadyUser = getAllRedayUserSet(reqMsg.getRoomNo());// 所有准备用户
				// 如果全部准备直接进行发牌
				if (allUser.size() <= allReadyUser.size() && allUser.size() > 1) {
					NnBean.RoomInfo.Builder nnRoom = getRoomInfo(reqMsg.getRoomNo());
					// 设置房间当前比赛场数
					nnRoom.setRoomCurMatchCount(nnRoom.getRoomCurMatchCount() + 1);
					RedisUtil.hset(NnConstans.NN_ROOM_PRE + reqMsg.getRoomNo(), "roomInfo", JsonFormat.printToString(nnRoom.build()));

					ServerManager.executor.execute(new NnWork(reqMsg, NnWrokEnum.SEND_CARD_RESULT.getCode(), ctx));
				}

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			} finally {
				redis.disconnect();
				redis.close();
			}
		}
	}

	/**
	 * 推送解散房间
	 * 
	 * @param reqMsg
	 */
	private static void batchSendClose(NnBean.ReqMsg reqMsg) {
		String roomNo = reqMsg.getRoomNo();
		Set<String> userSet = getAllUserSet(reqMsg.getRoomNo());

		NnBean.UserInfo.Builder userInfo = getCurUser(reqMsg.getUserId(), roomNo);

		userInfo.clearToken();
		userInfo.clearRoomNo();
		userInfo.clearIsWin();
		userInfo.clearIsreday();
		userInfo.clearIsGrabLandlord();
		userInfo.clearScoreType();
		userInfo.clearCard();

		Map<String, NnBean.RspMsg> map = new HashMap<String, NnBean.RspMsg>();

		NnBean.RspMsg.Builder rspMsg = NnBean.RspMsg.newBuilder();

		NnBean.RspData.Builder rspData = NnBean.RspData.newBuilder();

		rspData.setRestTime(getRestTime(reqMsg.getRoomNo()));
		rspMsg.setOperateType(NnPushMsgTypeEnum.EXIT_ROOM_PUSH.getCode());
		rspMsg.setCode(NnRspCodeEnum.$1101.getCode());
		rspMsg.setMsg(NnRspCodeEnum.$1101.getMsg());

		for (String key : userSet) {
			map.put(RedisUtil.hget(NnConstans.NN_USER_CHANNEL_PRE, key), rspMsg.build());
		}
		batchSendMsg(map);

	}

	private static void clearUserRedis(NnBean.ReqMsg reqMsg) {
		Set<String> set = getAllUserSet(reqMsg.getRoomNo());
		for (String key : set) {
			RedisUtil.hdel(NnConstans.NN_USER_CHANNEL_PRE, key);
		}

	}

	private static boolean clearRedis(NnBean.ReqMsg reqMsg) {
		Jedis redis = null;
		try {
			long roomId = getRoomId(reqMsg.getRoomNo());
			redis = RedisUtil.getJedis();
			Transaction tx = redis.multi();
			// 清楚点击开始缓存

			tx.hdel(NnConstans.NN_ROOM_USER_ALL_CARD_PRE, reqMsg.getRoomNo());// 删除卡缓存
			tx.hdel(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo());// 删除房间缓存
			tx.hdel(NnConstans.NN_REST_TIME_PRE, reqMsg.getRoomNo());// 删除房间缓存

			tx.del(NnConstans.NN_REST_TIME_PRE+reqMsg.getRoomNo());// 删除房间缓存
			tx.del(NnConstans.NN_ROOM_PRE + reqMsg.getRoomNo());// 删除房间缓存
			tx.del(NnConstans.NN_ROOM_TALK_PRE + reqMsg.getRoomNo());
			tx.del(NnConstans.ROOM_POSITION_PRE + reqMsg.getRoomNo());// 位置信息
			tx.del(NnConstans.NN_ROOM_USER_INFO_PRE + reqMsg.getRoomNo());// 清除该房间下的用户
			tx.del(NnConstans.NN_ROOM_LANDLORD_USER_PRE + reqMsg.getRoomNo());// 删除庄家缓存
			tx.del(NnConstans.NN_ROOM_FARMER_USER_PRE + reqMsg.getRoomNo());// 删除闲家缓存
			tx.del(NnConstans.NN_BUG_USER_PRE + reqMsg.getRoomNo());
			tx.del(NnConstans.NN_ROOM_ALL_READY_USER_PRE + reqMsg.getRoomNo());// 所有准备用户
			tx.del(NnConstans.NN_ROOM_ALL_USER_PRE + reqMsg.getRoomNo());// 所有用户
			tx.del(NnConstans.NN_ROOM_ALL_MATCH_USER_PRE + reqMsg.getRoomNo());// 所有用户
			tx.del(NnConstans.NN_ROOM_USER_REDAY_TIME_PRE + reqMsg.getRoomNo());// 删除准备到倒计时
			tx.exec();
			
			NnManagerDao.instance().closeRoom(roomId);
			NnManagerDao.instance().clearRoomNo(reqMsg.getRoomNo());
			NnManagerDao.instance().deleteRoomUser(reqMsg.getRoomNo(), GameTypeEnum.NIU_NIU.getType());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
			return false;
		} finally {
			redis.disconnect();
			redis.close();
		}

		return true;
	}

	/**
	 * 发送聊天内容
	 * 
	 * @param reqMsg
	 * @param ctx
	 */
	public static void sendTalkMsg(NnBean.ReqMsg reqMsg) {

		NnBean.RspMsg.Builder rspMsg = NnBean.RspMsg.newBuilder();

		NnBean.UserInfo.Builder user = getCurUser(reqMsg.getUserId(), reqMsg.getRoomNo());
		NnRoomUserTalkVo vo = new NnRoomUserTalkVo();
		vo.setUserId(reqMsg.getUserId());
		vo.setRoomNo(reqMsg.getRoomNo());
		vo.setRoomId(getRoomId(reqMsg.getRoomNo()));
		vo.setCreateDate(System.currentTimeMillis());

		NnPushMsgTypeEnum pushType = NnPushMsgTypeEnum.SEND_MSG_PUSH;
		if (reqMsg.getMsgType() == NnReqMsgTypeEnum.SEND_MSG.getCode()) {
			vo.setMsg(EmojiParser.removeAllEmojis(reqMsg.getTalkMsg()));
			vo.setVoiceType(NnTalkTypeEnum.TEXT.getCode());
			rspMsg.setOperateType(NnRspMsgTypeEnum.SEND_MSG_FEEDBACK.getCode());
			user.setTalkMsg(EmojiParser.removeAllEmojis(reqMsg.getTalkMsg()));
			pushType = NnPushMsgTypeEnum.SEND_MSG_PUSH;

		} else if (reqMsg.getMsgType() == NnReqMsgTypeEnum.SEND_APPOINT_VOICE.getCode()) {
			vo.setVoiceType(reqMsg.getVoiceType());
			vo.setVoiceType(NnTalkTypeEnum.APPOINT_VOICE.getCode());
			rspMsg.setOperateType(NnRspMsgTypeEnum.SEND_APPOINT_VOICE_FEEDBACK.getCode());
			user.setVoiceType(reqMsg.getVoiceType());
			pushType = NnPushMsgTypeEnum.SEND_APPOINT_VOICE_PUSH;
		} else if (reqMsg.getMsgType() == NnReqMsgTypeEnum.SEND_VOICE.getCode()) {
			vo.setVoice(reqMsg.getVoice().toByteArray());
			vo.setVoiceType(NnTalkTypeEnum.VOICE.getCode());
			rspMsg.setOperateType(NnRspMsgTypeEnum.SEND_VOICE_FEEDBACK.getCode());
			user.setVoice(reqMsg.getVoice());
			pushType = NnPushMsgTypeEnum.SEND_VOICE_PUSH;
		} else if (reqMsg.getMsgType() == NnReqMsgTypeEnum.SEND_EMOJI.getCode()) {
			vo.setMsg(reqMsg.getTalkMsg()+"");
			vo.setTalkType(NnTalkTypeEnum.EMOJI.getCode());
			rspMsg.setOperateType(NnRspMsgTypeEnum.SEND_EMOJI_FEEDBACK.getCode());
			user.setTalkMsg(reqMsg.getTalkMsg()+"");
			pushType = NnPushMsgTypeEnum.SEND_EMOJI_PUSH;
		}

		// 增加缓存数据
		RedisUtil.sadd(NnConstans.NN_ROOM_TALK_PRE + reqMsg.getRoomNo(), JSONObject.toJSONString(vo));

		RedisUtil.hset(NnConstans.NN_ROOM_USER_INFO_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "", JsonFormat.printToString(user.build()));

		batchSendTalk(reqMsg, pushType);

	}

	/**
	 * 聊天推送
	 * 
	 * @param reqMsg
	 * @param pushType
	 */
	private static void batchSendTalk(NnBean.ReqMsg reqMsg, NnPushMsgTypeEnum pushType) {

		String roomNo = reqMsg.getRoomNo();
		Set<String> userSet = getAllUserSet(reqMsg.getRoomNo());

		String curStatus = RedisUtil.hget(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo());

		NnBean.UserInfo.Builder userInfo = getCurUser(reqMsg.getUserId(), roomNo);

		userInfo.clearToken();
		userInfo.clearRoomNo();
		userInfo.clearIsWin();
		userInfo.clearIsreday();
		userInfo.clearIsGrabLandlord();
		userInfo.clearScoreType();
		userInfo.clearCard();

		NnBean.RoomInfo.Builder roomInfo = getRoomInfo(roomNo);

		Map<String, NnBean.RspMsg> map = new HashMap<String, NnBean.RspMsg>();

		NnBean.RspMsg.Builder rspMsg = NnBean.RspMsg.newBuilder();

		NnBean.RspData.Builder rspData = NnBean.RspData.newBuilder();

		rspData.setRestTime(getRestTime(reqMsg.getRoomNo()));

		rspData.setUser(userInfo);
		rspData.setCurRoomStatus(Integer.parseInt(curStatus));
		rspData.setRoom(roomInfo);
		rspMsg.setOperateType(pushType.getCode());
		rspMsg.setData(rspData);
		rspMsg.setCode(NnRspCodeEnum.$0000.getCode());
		rspMsg.setMsg(NnRspCodeEnum.$0000.getMsg());

		for (String key : userSet) {
			map.put(RedisUtil.hget(NnConstans.NN_USER_CHANNEL_PRE, key), rspMsg.build());

		}
		batchSendMsg(map);

	}

	/**
	 * 剔除房间用户
	 * 
	 * @param json
	 */
	public static void takeOutRoomUser(String json) {
		try {
			logger.info("剔除用户{}", json);
			if (StringUtils.isEmpty(json)) {
				return;
			}
			JSONObject jb = JSONObject.parseObject(json);
			String roomNo = (String) jb.get("roomNo");
			long userId = Long.parseLong(jb.get("userId") + "");

			String channelId = RedisUtil.hget(NnConstans.NN_USER_CHANNEL_PRE, userId + "");

			NnBean.RspMsg.Builder rspMsg = NnBean.RspMsg.newBuilder();
			NnBean.UserInfo.Builder user = getCurUser(userId, roomNo);

			NnBean.RoomInfo.Builder roomInfo = getRoomInfo(roomNo);
			ReqMsg reqMsg = ReqMsg.newBuilder().setRoomNo(roomNo).setUserId(userId).build();
			roomInfo.setRoomCurPersonCount(roomInfo.getRoomCurPersonCount() - 1);// 设置当前人数
			roomInfo.setRoomCurStatus(NnYesNoEnum.NO.getCode());
			RedisUtil.hset(NnConstans.NN_ROOM_PRE + roomInfo.getRoomNo(), "roomInfo", JsonFormat.printToString(roomInfo.build()));
			Jedis redis = null;
			try {
				redis = RedisUtil.getJedis();
				Transaction tx = redis.multi();
				NnBean.RspData.Builder rspData = NnBean.RspData.newBuilder();
				NnBean.UserInfo.Builder curUser = NnBean.UserInfo.newBuilder();
				curUser.setPosition(user.getPosition());
				rspData.setUser(curUser);

				tx.hdel(NnConstans.NN_ROOM_USER_INFO_PRE + roomNo, userId + "");
				tx.srem(NnConstans.NN_ROOM_ALL_MATCH_USER_PRE + roomNo, userId + "");// 所有比赛用户
				tx.srem(NnConstans.NN_ROOM_ALL_READY_USER_PRE + roomNo, userId + "");// 所有准备用户
				tx.srem(NnConstans.NN_ROOM_ALL_USER_PRE + roomNo, userId + "");// 所有用户
				tx.hdel(NnConstans.NN_USER_CHANNEL_PRE, userId + "");// 删除渠道缓存
				tx.hdel(NnConstans.NN_BUG_USER_PRE + roomInfo.getRoomNo(), userId + "");
				tx.hdel(NnConstans.NN_ROOM_USER_REDAY_TIME_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "");
				tx.exec();

				delUserPostion(userId, roomNo);
				NnManagerDao.instance().deleteRoomUser(userId, roomNo, GameTypeEnum.NIU_NIU.getType());
				LocalCacheUtil.hdel(NnConstans.NN_CLOSE_ROOM_LOCK_REQ, reqMsg.getRoomNo()+"");
				rspMsg.setData(rspData);
				rspMsg.setCode(NnRspCodeEnum.$1101.getCode());
				rspMsg.setMsg(NnRspCodeEnum.$1101.getMsg());
				sendMsg(rspMsg.build(), channelId);// 推送单个

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			} finally {
				redis.disconnect();
				redis.close();
			}

			Map<String, NnBean.RspMsg> map = new HashMap<String, NnBean.RspMsg>();

			Set<String> allUserSet = getAllUserSet(roomNo);
			for (String key : allUserSet) {
				rspMsg.setOperateType(NnPushMsgTypeEnum.EXIT_ROOM_PUSH.getCode());
				rspMsg.setCode(NnRspCodeEnum.$0000.getCode());
				rspMsg.setMsg(NnRspCodeEnum.$0000.getMsg());
				map.put(RedisUtil.hget(NnConstans.NN_USER_CHANNEL_PRE, key), rspMsg.build());
			}
			batchSendMsg(map);
			
			//如果房间没人直接解散房间
			if (roomInfo.getRoomCurPersonCount() <= 0) {
				clearUserRedis(reqMsg);
				clearRedis(reqMsg);
				return;
			}
			
			if(roomInfo.getRoomCurPersonCount()<=1){
				return;
			}
			// 用户退出后判断其他用户是否全部准备，如果全部准备，则进行发牌操作,判断当前房间状态
			String roomStatus = RedisUtil.hget(NnConstans.NN_ROOM_CUR_STATUS_PRE, roomNo);

			
			if (Integer.parseInt(roomStatus) == NnRoomMatchStatusEnum.SHOW_MATCH_RESULT_STATUS.getCode() || Integer.parseInt(roomStatus) == NnRoomMatchStatusEnum.INIT_HOME_STATUS.getCode()
					|| Integer.parseInt(roomStatus) == NnRoomMatchStatusEnum.REDAY_STATUS.getCode()) {
				Set<String> allUser = getAllUserSet(reqMsg.getRoomNo());// 所有用户
				Set<String> allReadyUser = getAllRedayUserSet(reqMsg.getRoomNo());// 所有准备用户
				// 如果全部准备直接进行发牌
				if (allUser.size() <= allReadyUser.size()&&allReadyUser.size()>1) {
					roomInfo = getRoomInfo(roomNo);
					// 设置房间当前比赛场数
					roomInfo.setRoomCurMatchCount(roomInfo.getRoomCurMatchCount() + 1);
					RedisUtil.hset(NnConstans.NN_ROOM_PRE + reqMsg.getRoomNo(), "roomInfo", JsonFormat.printToString(roomInfo.build()));
					redaySendCard(reqMsg);
				}

			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();

		}
	}

	/**
	 * 用户房间初始化
	 * 
	 * @param reqMsg
	 */
	public static void initRoom(NnBean.ReqMsg reqMsg, ChannelHandlerContext ctx) {

		NnBean.RspMsg.Builder rspMsg = NnBean.RspMsg.newBuilder();
		NnBean.RspData.Builder rspData = NnBean.RspData.newBuilder();

		// 用户重新进入系统后，如果上局比赛未结束，进行数据初始化操作
		// 1.判断用户是否还在房间中
		if (!RedisUtil.sismember(NnConstans.NN_ROOM_ALL_USER_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "")) {
			rspMsg.setOperateType(NnRspMsgTypeEnum.INIT_ROOM_FEEDBACK.getCode());
			rspMsg.setCode(NnRspCodeEnum.$0003.getCode());
			rspMsg.setMsg(NnRspCodeEnum.$0003.getMsg());
			pushsingle(rspMsg.build(), ctx);
			return;
		}

		Set<String> userSet = getAllUserSet(reqMsg.getRoomNo());

		String curStatus = RedisUtil.hget(NnConstans.NN_ROOM_CUR_STATUS_PRE, reqMsg.getRoomNo());
		if (StringUtils.isEmpty(curStatus)) {
			curStatus = "";
		}
		// -----------------------------------------------封装房间用户信息------------------------------------------------------
		NnBean.RoomInfo.Builder roomInfo = getRoomInfo(reqMsg.getRoomNo());

		// --------------------------------------------设置用户信息--------------------------------------------------
		NnBean.UserInfo.Builder user = getCurUser(reqMsg.getUserId(), reqMsg.getRoomNo());

		long clubGolds = NnManagerDao.instance().getUserGold(reqMsg.getUserId());
		user.setTotalGold((int) clubGolds);
		RedisUtil.hset(NnConstans.NN_ROOM_USER_INFO_PRE + reqMsg.getRoomNo(), reqMsg.getUserId() + "", JsonFormat.printToString(user.build()));

		if (!curStatus.equals(NnRoomMatchStatusEnum.SHOW_MATCH_LAST_RESULT_STATUS.getCode() + "")) {
			List<NnBean.UserInfo> listUser = new ArrayList<NnBean.UserInfo>();
			for (String userKey : userSet) {

				if (Long.parseLong(userKey) != reqMsg.getUserId()) {

					NnBean.UserInfo.Builder otherUser = getCurUser(Long.parseLong(userKey), reqMsg.getRoomNo());
					otherUser.clearToken();
					otherUser.clearRoomNo();
					otherUser.clearBaseScore();
					if (otherUser.getIsShowCard() == NnYesNoEnum.YES.getCode()) {

					} else {
						otherUser.clearCard();
					}

					if (otherUser.getIsreday() == NnYesNoEnum.NO.getCode()) {
						otherUser.setRedayTime(getRedayTime(reqMsg.getRoomNo(), String.valueOf(otherUser.getUserId())));
					}

					listUser.add(otherUser.build());

				}

			}
			rspData.addAllOtherUser(listUser);
		}
		if (curStatus.equals(NnRoomMatchStatusEnum.SHOW_MATCH_RESULT_STATUS.getCode() + "")) {
			if (RedisUtil.hexists(NnConstans.NN_ROOM_USER_RNK, reqMsg.getRoomNo())) {
				NnBean.resultRank.Builder userRank = getUserRank(reqMsg.getRoomNo());
				rspData.addAllRank(userRank.getUserRankList());
				rspData.setMatchEndDate(userRank.getMatchEndDate());
			}

		}
		
		int restTime = getRestTime(reqMsg.getRoomNo());

		user.setRedayTime(getRedayTime(reqMsg.getRoomNo(), String.valueOf(user.getUserId())));
		user.setIp(ctx.channel().remoteAddress().toString().replaceAll("(/)|:(.*)", ""));
		rspData.setUser(user);

		// 设置房间信息

		roomInfo.clearRoomCurPersonCount();
		roomInfo.clearCardDouble();
		rspData.setRestTime(restTime);
		rspData.setRoom(roomInfo);
		rspData.setCurRoomStatus(Integer.parseInt(curStatus));

		rspMsg.setOperateType(NnRspMsgTypeEnum.INIT_ROOM_FEEDBACK.getCode());
		rspMsg.setData(rspData);
		rspMsg.setCode(NnRspCodeEnum.$0000.getCode());
		rspMsg.setMsg(NnRspCodeEnum.$0000.getMsg());
		pushsingle(rspMsg.build(), ctx);
	}

	/**
	 * 获取游戏超时时间
	 * 
	 * @param reqMsg
	 * @return
	 */
	private static GameTimoutVo getGameTimoutVo(String roomNo) {

		String str = RedisUtil.hget(NnConstans.NN_REST_TIME_PRE, roomNo);

		if (str != null) {
			return JSONObject.parseObject(str, GameTimoutVo.class);
		}
		return new GameTimoutVo();
	}

	/**
	 * 系统初始化
	 * @param roomNo
	 */
	public static void sysDataInit(String roomNo){
		try {
			
			//判断当前房间号是否存在
		
			if(!RedisUtil.exists(NnConstans.NN_ROOM_PRE+roomNo)){
				return;
			}
			NnBean.ReqMsg.Builder reqMsg=NnBean.ReqMsg.newBuilder();
			reqMsg.setRoomNo(roomNo);
			int restTime=getRestTime(roomNo);
			GameTimoutVo timeVo=getGameTimoutVo(roomNo);
			
			if(timeVo.getRestTimeType()==NnTimeTaskEnum.GRAB_LANDLORD.getCode()){
				// 增加个人准备倒计时
				ServerManager.executorTask.schedule(new MyTimerTask(reqMsg.build(), NnTimeTaskEnum.GRAB_LANDLORD.getCode(),timeVo.getRestTime()),restTime, TimeUnit.SECONDS);
				
			}else if(timeVo.getRestTimeType()==NnTimeTaskEnum.FARMER_DOUBLEX.getCode()){
				
				ServerManager.executorTask.schedule(new MyTimerTask(reqMsg.build(), NnTimeTaskEnum.FARMER_DOUBLEX.getCode(),timeVo.getRestTime()), restTime, TimeUnit.SECONDS);
				
			}else if(timeVo.getRestTimeType()==NnTimeTaskEnum.SHOW_CARD_RESULT.getCode()){
				
				ServerManager.executorTask.schedule(new MyTimerTask(reqMsg.build(), NnTimeTaskEnum.SHOW_CARD_RESULT.getCode(),timeVo.getRestTime()), restTime, TimeUnit.SECONDS);
				
			}
			
			Set<String> allUserSet=getAllUserSet(roomNo);
			
			for(String uid:allUserSet){
				NnBean.ReqMsg.Builder newReqMsg=NnBean.ReqMsg.newBuilder();
				reqMsg.setRoomNo(roomNo);
				
				GameTimoutVo utimeVo= getUserTimeVo(reqMsg.getRoomNo(), reqMsg.getUserId()+"");
				int redayTime=getRedayTime(roomNo, uid);
				ServerManager.executorTask.schedule(new MyTimerTask(newReqMsg.build(), NnTimeTaskEnum.USER_MATCH_END_REDAY.getCode(),utimeVo.getRestTime()),redayTime, TimeUnit.SECONDS);
			} 
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return;
	}
	
	/**
	 * 发送消息
	 * 
	 * @param map
	 */
	public static void sendMsg(NnBean.RspMsg rspMsg, String channelId) {
		System.out.println();
		try {
			for (Channel channel : ServerManager.channels) {
				if (channel.id().asLongText().equalsIgnoreCase(channelId)) {
					if (!channel.isActive() || !channel.isOpen()) {
						logger.info("该渠道已关闭,推送channeId:" + channel.id().asLongText() + ":操作类型=" + rspMsg.getOperateType() + ":推送信息长度=" + rspMsg.toByteArray().length + ":响应码=" + rspMsg.getCode());
						channel.close();
					} else {
						logger.info("推送channeId:" + channel.id().asLongText() + ":操作类型=" + rspMsg.getOperateType() + ":推送信息长度=" + rspMsg.toByteArray().length + ":响应码=" + rspMsg.getCode());
						channel.writeAndFlush(rspMsg).addListener(new ChannelFutureListener() {
							@Override
							public void operationComplete(ChannelFuture channelFuture) throws Exception {
								if (!channelFuture.isSuccess()) {
									System.out.println(">>>>>>>>>>>>>>>>>>>>>>发送消息错误");
									channelFuture.cause().printStackTrace();
									channelFuture.channel().close();
								}
							}
						});
					}

					break;
				}
			}
		} catch (Exception ce) {
			logger.error(ce.getMessage(), ce);
			ce.printStackTrace();
		}

	}

	/**
	 * 多人发送消息
	 * 
	 * @param map
	 */
	public static void batchSendMsg(Map<String, NnBean.RspMsg> map) {
		System.out.println();
		try {
			for (String key : map.keySet()) {
				for (Channel channel : ServerManager.channels) {
					if (channel.id().asLongText().equalsIgnoreCase(key)) {

						if (!channel.isActive() || !channel.isOpen()) {
							logger.info("该渠道已关闭,推送channeId:" + channel.id().asLongText() + ":操作类型=" + map.get(key).getOperateType() + ":推送信息长度=" + map.get(key).toByteArray().length + ":响应码=" + map.get(key).getCode());
							channel.close();
						} else {
							// logger.info(map);
							logger.info("推送channeId:" + channel.id().asLongText() + ":操作类型=" + map.get(key).getOperateType() + ":推送信息长度=" + map.get(key).toByteArray().length + ":响应码=" + map.get(key).getCode());
							channel.writeAndFlush(map.get(key)).addListener(new ChannelFutureListener() {
								@Override
								public void operationComplete(ChannelFuture channelFuture) throws Exception {
									if (!channelFuture.isSuccess()) {
										System.out.println(">>>>>>>>>>>>>>>>>>>>>>发送消息错误");
										channelFuture.cause().printStackTrace();
										channelFuture.channel().close();
									}
								}
							});
						}

						break;
					}
				}
			}
		} catch (Exception ce) {
			logger.error(ce.getMessage(), ce);
			ce.printStackTrace();
		}

	}

	// 设置位置信息
	private static int getPostion(Long userId, String roomNo) {
		String[] POSITION = new String[10];
		String str = RedisUtil.get(NnConstans.ROOM_POSITION_PRE + roomNo);
		if (StringUtils.isNotEmpty(str)) {
			POSITION = str.split(",");
		}

		int posi = 1;
		boolean isExist = false;
		for (int i = 0; i < POSITION.length; i++) {
			if (null != POSITION[i] && POSITION[i].equals(String.valueOf(userId))) {
				posi = i + 1;
				isExist = true;
				break;
			}
		}
		if (isExist) {
			return posi;
		}
		for (int i = 0; i < POSITION.length; i++) {

			if (StringUtils.isEmpty(POSITION[i]) || POSITION[i].equals("null")) {
				POSITION[i] = (userId + "");
				posi = i + 1;
				break;
			}

		}
		StringBuffer sb = new StringBuffer();
		for (String po : POSITION) {
			sb.append(po + ",");
		}
		RedisUtil.set(NnConstans.ROOM_POSITION_PRE + roomNo, sb.substring(0, sb.length() - 1));
		return posi;
	}

	/**
	 * 清楚位置信息
	 * 
	 * @param userId
	 * @param roomNo
	 */
	private static void delUserPostion(Long userId, String roomNo) {

		String[] POSITION = new String[10];
		String str = RedisUtil.get(NnConstans.ROOM_POSITION_PRE + roomNo);
		if (StringUtils.isNotEmpty(str)) {
			POSITION = str.split(",");
		}

		for (int i = 0; i < POSITION.length; i++) {

			if (POSITION[i].equals(userId.toString())) {
				POSITION[i] = "";
				break;
			}

		}
		StringBuffer sb = new StringBuffer();
		for (String po : POSITION) {
			sb.append(po + ",");
		}
		RedisUtil.set(NnConstans.ROOM_POSITION_PRE + roomNo, sb.substring(0, sb.length() - 1));

	}

}
