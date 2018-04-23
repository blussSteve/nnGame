package com.lhyone.nn.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.lhyone.nn.dao.NnManagerDao;
import com.lhyone.nn.entity.NnRoomMultipleDic;
import com.lhyone.nn.entity.SysAgentIncomeConfig;
import com.lhyone.nn.enums.PropEnums;
import com.lhyone.nn.vo.PropDicVo;
import com.lhyone.util.RedisUtil;

public class NnUtil {
	
	/**庄家加注积分*/
	private final static Integer[] LANDLORD_SCORE_MENU={0,1,2,3,4};
	/**闲家加注积分*/
	private final static Integer[] FARMER_SCORE_MENU={2,3,4,5,6};
	
	private static List<NnRoomMultipleDic>  getListNnRoomMultipleDic(){
		String str=RedisUtil.get(NnConstans.NN_ROOM_MULTIPLE_DIC_PRE);
		if(StringUtils.isEmpty(str)){
			List<NnRoomMultipleDic> list=NnManagerDao.instance().queryNnRoomMultipleDic();
			RedisUtil.set(NnConstans.NN_ROOM_MULTIPLE_DIC_PRE,JSONObject.toJSONString(list));
			
			return list;
		}
		
		return 	JSONObject.parseArray(str, NnRoomMultipleDic.class);
	}
	
	/**
	 * 获取道具卡规则
	 * @return
	 */
	private static List<PropDicVo> getPropDicVo(){
		
		String str=RedisUtil.get(NnConstans.PROP_DIC_PRE);
		if(StringUtils.isEmpty(str)){
			List<PropDicVo> list=new ArrayList<PropDicVo>();
		  for (PropEnums enums : PropEnums.values()) {
			 	PropDicVo pdv=new PropDicVo();
			 	pdv.setNum(enums.getNum());
			 	pdv.setName(enums.getName());
			 	pdv.setCostGold(enums.getCostGold());
			 	list.add(pdv);
	        }
			
			RedisUtil.set(NnConstans.PROP_DIC_PRE,JSONObject.toJSONString(list));
			
			return list;
		}
		
		return 	JSONObject.parseArray(str, PropDicVo.class);
		
	}
	
	/**
	 * 获取特殊牌型倍数
	 * @param type
	 * @return
	 */
	public static int getCardTypeSpecialDouble(int type){
		
		for(NnRoomMultipleDic bean:getListNnRoomMultipleDic()){
			if(bean.getNum()==type){
				
				return bean.getSpecialMultiple();
			}
			
		}
		return 1;
	}
	
	/**
	 * 获取特殊牌型倍数
	 * @param type
	 * @return
	 */
	public static int getCardTypeDouble(int type){
		
		for(NnRoomMultipleDic bean:getListNnRoomMultipleDic()){
			if(bean.getNum()==type){
				
				return bean.getMultiple();
			}
			
		}
		return 1;
	}
	
	/**
	 * 获取道具卡信息
	 * @param num
	 * @return
	 */
	public static PropDicVo getPropDicVoByNum(int num){
		
		for(PropDicVo bean:getPropDicVo()){
			if(bean.getNum()==num){
				
				return bean;
			}
			
		}
		return null;
	}
	
	 
	/**
	 * 检查庄家加注是否正确
	 * @param score
	 * @return
	 */
	public static boolean checkLandlordScoreIsTrue(int score){
		List<Integer> landlord=getLandlordScoreList();
		if(landlord.contains(score)){
			return true;
		}
		return false;
	}
	/**
	 * 获取庄家积分菜单最小值
	 * @return
	 */
	public static int getLandlordMenuMinScore(){
		List<Integer> landlord=getLandlordScoreList();
		return landlord.get(0);
	}
	
	/**
	 * 获取庄家积分菜单
	 * @return
	 */
	private static List<Integer> getLandlordScoreList(){
		List<Integer> landlord=null;
		String str=RedisUtil.hget(NnConstans.NN_SCORE_MENU_CASHE_PRE, "landlord");
		if(!RedisUtil.hexists(NnConstans.NN_SCORE_MENU_CASHE_PRE, "landlord")){
			landlord=Arrays.asList(LANDLORD_SCORE_MENU);
			str=JSONObject.toJSONString(landlord);
			RedisUtil.hset(NnConstans.NN_SCORE_MENU_CASHE_PRE, "landlord",str );
			
		}else{
			landlord=JSONObject.parseArray(str,Integer.class);
		}
		return landlord;
	}
	
	/**
	 * 检查闲家加注是否正确
	 * @param score
	 * @return
	 */
	public static boolean checkFarmerScoreIsTrue(int score){
		List<Integer> farmer=getFarmerScoreList();
		if(farmer.contains(score)){
			return true;
		}
		return false;
	}

	/**
	 * 获取闲家积分菜单最小值
	 * @return
	 */
	public static int getFarmerMenuMinScore(){
		List<Integer> farmer=getFarmerScoreList();
		return farmer.get(0);
	}
	
	/**
	 * 获取闲家积分菜单
	 * @return
	 */
	private static List<Integer> getFarmerScoreList(){
		List<Integer> farmer=null;
		String str=RedisUtil.hget(NnConstans.NN_SCORE_MENU_CASHE_PRE, "farmer");
		if(!RedisUtil.hexists(NnConstans.NN_SCORE_MENU_CASHE_PRE, "farmer")){
			farmer=Arrays.asList(FARMER_SCORE_MENU);
			str=JSONObject.toJSONString(farmer);
			RedisUtil.hset(NnConstans.NN_SCORE_MENU_CASHE_PRE, "farmer",str);
			
		}else{
			farmer=JSONObject.parseArray(str,Integer.class);
		}
		return farmer;
	}
	
	
	public static void main(String[] args) {
		boolean flag=RedisUtil.exists(NnConstans.NN_ROOM_PRE + "203973");
		System.out.println(flag);
	}
	
}
