package com.lhyone.nn.util;

public class NnConstans {
	
	/**准备*/
	public final static int USER_RDAY_TIME=15;
	/**抢庄时间*/
	public final static int GRAB_LANDLORD_TIME=15;
	/**闲家加倍时间*/
	public final static int FARMER_TIME=15;
	/**打牌时间*/
	public final static int PLAY_CARD_TIME=15;
	/**展示比赛结果时间*/
	public final static int SHOW_MATCH_TIME=15;
	/**牛牛渠道*/
	public final static String NN_CHANNEL_PRE="nn_channel:";
	/**牛牛用户*/
	public final static String NN_USER_CHANNEL_PRE="nn_user_channel:";
	/**房间位置信息*/
	public final static String ROOM_POSITION_PRE="nn_room_position_";
	/**房间信息*/
	public final static String NN_ROOM_PRE="nn_room:";
	/**房间用户缓存*/
	public final static String NN_ROOM_USER_INFO_PRE="nn_room_user_info:";
	/**用户所有卡的缓存*/
	public final static String NN_ROOM_USER_ALL_CARD_PRE="nn_room_user_all_card:";
	/**用户抢庄缓存*/
	public final static String NN_ROOM_LANDLORD_USER_PRE="nn_room_landlord_user:";
	/**用户闲家积分缓存*/
	public final static String NN_ROOM_FARMER_USER_PRE="nn_room_farmer_user:";
	/**当前比赛状态*/
	public final static String NN_ROOM_CUR_STATUS_PRE="nn_room_status:";
	
	/**牛牛房间用户每个人准备的倒计时*/
	public final static String NN_ROOM_USER_REDAY_TIME_PRE="nn_room_user_reday:";//
	
	/**用户redis的key前缀*/
    public static final String REDIS_USER_PRE="user:";
    
    
    /**牛牛房间聊天缓存*/
    public static final String NN_ROOM_TALK_PRE="nn_room_talk:";
    
    /**房间规则字典*/
    public static final String NN_ROOM_MULTIPLE_DIC_PRE="nn_room_multiple_dic";
    
    /**牛牛房间bug用户*/
    public static final String NN_BUG_USER_PRE="nn_room_bug_user:";
    
    /**牛牛倒计时*/
    public static final String NN_REST_TIME_PRE="nn_rest_time:";
    
    /**牛牛所有用户*/
    public static final String NN_ROOM_ALL_USER_PRE="nn_room_all_user:";
    
    /**牛牛所有准备用户*/
    public static final String NN_ROOM_ALL_READY_USER_PRE="nn_room_all_ready_user:";
    
    /**牛牛所有比赛用户*/
    public static final String NN_ROOM_ALL_MATCH_USER_PRE="nn_room_all_match_user:";
    
    public static final String PROP_DIC_PRE="prop_dic";
    
	/**牛牛线程锁，防止高并发用*/
	public static final String NN_ROOM_LOCK_REQ="nn_room_lock";
	
	/**牛牛用户线程锁，防止高并发用*/
	public static final String NN_USER_LOCK_REQ="nn_user_lock";
	
	public static final String NN_CLOSE_ROOM_LOCK_REQ="nn_close_room_lock";
	
	/**牛牛防止并发缓存*/
	public static final String NN_USER_THREAD_LOCK_CACHE_PRE="nn_user_thread_cache";
	
	/**牛牛积分菜单缓存*/
	public static final String NN_SCORE_MENU_CASHE_PRE="nn_score_menu:";
	
	/**牛牛代理商收益*/
	public static final String NN_ROOM_USER_RNK="nn_room_user_rank_cache:";
	/**系统当前时间*/
	public static final String NN_SYS_CUR_TIME_CACHE="nn_sys_cur_time";
}
