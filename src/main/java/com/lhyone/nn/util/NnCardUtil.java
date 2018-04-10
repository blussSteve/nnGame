package com.lhyone.nn.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.lhyone.nn.enums.NnCardNumEnum;
import com.lhyone.nn.enums.NnCardPointEnum;
import com.lhyone.nn.enums.NnCardRuleEnum;
import com.lhyone.nn.enums.NnCardTypeEnum;
import com.lhyone.nn.enums.NnCardTypeTransEnum;


public class NnCardUtil {
	
	
	private final static List<Integer> staticCardList=new ArrayList<Integer>();
	
	/**胜率基数*/
	private final static long WIN_RATE_BASE=10000;
	
	private final static int cardCount=5;
	
	private final static int fourCardCount=4;
	static{
		for (NnCardTypeEnum cardRule : NnCardTypeEnum.values()) {
			   for (NnCardNumEnum cardNum : NnCardNumEnum.values()) {
				   staticCardList.add(Integer.parseInt(cardNum.getCode()+""+cardRule.getCode()));
		      }
        }
	}
	private static List<Integer> getCardList(){
		return new ArrayList<Integer>(staticCardList);
	}
	
	public static Map<String, List<Integer>> getSimpleCard(String... serNos){
		return getCard(null, null,null,serNos);
	}
	
	public static Map<String, List<Integer>> getSimpleSameCard(String... serNos){
		return getSameCard(null, null,null,serNos);
	}
	/**
	 * 发牌
	 * @param point 权重
	 * @param pointSerNo 权重用户
	 * @param serNos 用户
	 * @return
	 */
	public static Map<String, List<Integer>> getCard(String point,String pointSerNo,Integer winRate,String... serNos){
		List<Integer> cardList=getCardList();
		if(serNos.length==0||serNos.length>10){
			return null;
		}
		Map<String,List<Integer>> map=new HashMap<String,List<Integer>>();
		Map<String,List<Integer>> newMap=new HashMap<String,List<Integer>>();
		for(int i=0;i<30;i++){
			Collections.shuffle(cardList); 
		}
		if(NnCardPointEnum.B.equals(point)){
			
		}else if(NnCardPointEnum.C.name().equals(point)){
			 
		}else if(NnCardPointEnum.E.name().equals(point)){
			 
		}else if(NnCardPointEnum.F.name().equals(point)){
			//bug模式规则,先确定玩家牌型,然后再设置bug用户牌型
			
			for(int j=0;j<serNos.length;j++){
				List<Integer> cards=new ArrayList<Integer>(cardList.subList(j*cardCount, (j+1)*cardCount));
				map.put(serNos[j],cards);
			}
			
//			List<Integer> myCards= map.get(pointSerNo);
			List<Integer> maxCards=null;
			List<Integer> minCards=null;
			String winUserId=pointSerNo;
			String loseUserId=pointSerNo;
			for(String key:map.keySet()){
				List<Integer> cards=map.get(key);
				if(maxCards==null||compareCard(cards,maxCards)){
					maxCards=cards;
					winUserId=key;
				}
				if(minCards==null||!compareCard(cards,minCards)){
					minCards=cards;
					loseUserId=key;
				}
			}
			
		  boolean flag= isWin(winRate);
		  //如果必须是赢的话做处理
		  if(flag){
			  
			  if(!winUserId.equals(pointSerNo)){
				  List<Integer> otherAllCards=new ArrayList<Integer>();
				  otherAllCards=cardList.subList((serNos.length+1)*cardCount, cardList.size());
				  
				  List<Integer> myNewCards = getWinNewCards(new ArrayList<Integer>(map.get(winUserId)), otherAllCards);
				  if(null!=myNewCards){
					  map.put(pointSerNo, myNewCards);
				  }
				  
			  }
		  }else{
//			  if(!loseUserId.equals(pointSerNo)){
//				  List<Integer> otherAllCards=new ArrayList<Integer>();
//				  otherAllCards=cardList.subList((serNos.length+1)*cardCount, cardList.size());
//				  System.out.println("===================:"+map.get(loseUserId));
//				  List<Integer> myNewCards = getLoseNewCards(new ArrayList<Integer>(map.get(loseUserId)), otherAllCards);
//				  if(null!=myNewCards){
//					  map.put(pointSerNo, myNewCards);
//				  }
//				  
//			  }
			  
		  }
			
			 
		}else {
			
			for(int j=0;j<serNos.length;j++){
				map.put(serNos[j],cardList.subList(j*cardCount, (j+1)*cardCount));
			}
			 
		}
		
		for(String key:map.keySet()){
			List<Integer> myCardList=map.get(key);
			newMap.put(key, myCardList);
		}
		return newMap;
	}
	
	
	/**
	 * 发牌
	 * @param point 权重
	 * @param pointSerNo 权重用户
	 * @param serNos 用户
	 * @return
	 */
	public static Map<String, List<Integer>> getSameCard(String point,String pointSerNo,Integer winRate,String... serNos){
		List<Integer> cardList=getCardList();
		if(serNos.length==0||serNos.length>10){
			return null;
		}
		Map<String,List<Integer>> map=new HashMap<String,List<Integer>>();
		Map<String,List<Integer>> newMap=new HashMap<String,List<Integer>>();
		for(int i=0;i<30;i++){
			Collections.shuffle(cardList); 
		}
		if(NnCardPointEnum.B.equals(point)){
			
		}else if(NnCardPointEnum.C.name().equals(point)){
			 
		}else if(NnCardPointEnum.E.name().equals(point)){
			 
		}else if(NnCardPointEnum.F.name().equals(point)){
			//bug模式规则,先确定玩家牌型,然后再设置bug用户牌型
			
			Integer sameCard =cardList.get((int)(Math.random()*(cardList.size())));
			cardList.remove(sameCard);
			for(int j=0;j<serNos.length;j++){
				List<Integer> cards=new ArrayList<Integer>( cardList.subList(j*fourCardCount, (j+1)*fourCardCount));
				cards.add(sameCard);
				map.put(serNos[j],cards);
			}
			
//			List<Integer> myCards= map.get(pointSerNo);
			List<Integer> maxCards=null;
			List<Integer> minCards=null;
			String winUserId=pointSerNo;
			String loseUserId=pointSerNo;
			for(String key:map.keySet()){
				List<Integer> cards=map.get(key);
				if(maxCards==null||compareCard(cards,maxCards)){
					maxCards=cards;
					winUserId=key;
				}
				if(minCards==null||!compareCard(cards,minCards)){
					minCards=cards;
					loseUserId=key;
				}
			}
			
		  boolean flag= isWin(winRate);
		  //如果必须是赢的话做处理
		  if(flag){
			  
			  if(!winUserId.equals(pointSerNo)){
				  List<Integer> otherAllCards=new ArrayList<Integer>();
				  otherAllCards=cardList.subList((serNos.length+1)*cardCount, cardList.size());
				  
				  List<Integer> myNewCards = getSameWinNewCards(new ArrayList<Integer>(map.get(winUserId)), otherAllCards,sameCard);
				  if(null!=myNewCards){
					  map.put(pointSerNo, myNewCards);
				  }
				  
			  }
		  }else{
//			  if(!loseUserId.equals(pointSerNo)){
//				  List<Integer> otherAllCards=new ArrayList<Integer>();
//				  otherAllCards=cardList.subList((serNos.length+1)*cardCount, cardList.size());
//				  System.out.println("===================:"+map.get(loseUserId));
//				  List<Integer> myNewCards = getSameLoseNewCards(new ArrayList<Integer>(map.get(loseUserId)), otherAllCards,sameCard);
//				  if(null!=myNewCards){
//					  map.put(pointSerNo, myNewCards);
//				  }
//				  
//			  }
			  
		  }
			
			 
		}else {
			
			Integer sameCard =cardList.get((int)(Math.random()*(cardList.size())));
			cardList.remove(sameCard);
			for(int j=0;j<serNos.length;j++){
				List<Integer> cards=new ArrayList<Integer>( cardList.subList(j*fourCardCount, (j+1)*fourCardCount));
				cards.add(sameCard);
				map.put(serNos[j],cards);
			}
			 
		}
		
		for(String key:map.keySet()){
			List<Integer> myCardList=map.get(key);
			newMap.put(key, myCardList);
		}
		return newMap;
	}
	
	
	
	/**
	 * 获取牛
	 * @param myCards
	 * @return
	 */
    public static List<Integer> getNiuCards(List<Integer> myCards){
         if (isNiuNiu(myCards) > 0){
            return getNiuNiuCards(myCards);
        }
        else if (isNiuDan(myCards) > 0){
            return getNiuDanCards(myCards);
        }
         return null;
    }
	
	/**
	 * 比较大小
	 * @param myCards
	 * @param otherCards
	 * @return
	 */
	public static boolean compareCard(List<Integer> myCards,List<Integer> otherCards){
		myCards=new ArrayList<Integer>(myCards);
		otherCards=new ArrayList<Integer>(otherCards);
		Collections.sort(myCards,new Comparator<Integer>(){
            public int compare(Integer org, Integer dest ) {
                return org.compareTo(dest);
            }
        });
		
		Collections.sort(otherCards,new Comparator<Integer>(){
            public int compare(Integer org, Integer dest ) {
                return org.compareTo(dest);
            }
        });
		 Map<String,Object> myCardRuleMap=getCardRule(myCards);
		 
		NnCardRuleEnum myCardRule=(NnCardRuleEnum) myCardRuleMap.get("cardRuleEnum"); 
		int myBigCard= (Integer)myCardRuleMap.get("card");
		
		Map<String,Object> otherCardMap=getCardRule(otherCards);
		
		NnCardRuleEnum otherCardRule=(NnCardRuleEnum) otherCardMap.get("cardRuleEnum");
		int otherBigCard=(Integer)otherCardMap.get("card");
		if(myCardRule==otherCardRule){
			if(myBigCard==otherBigCard){
				return getMaxCard(myCards)>getMaxCard(otherCards);
			}
			return myBigCard>otherBigCard;
		}else{
			return myCardRule.getCode()>otherCardRule.getCode();
		}
	}
	
	/**
	 * 比较大小
	 * @param myCards
	 * @param otherCards
	 * @return
	 */
	public static boolean compareFuGuiCard(List<Integer> myCards,List<Integer> otherCards){
		myCards=new ArrayList<Integer>(myCards);
		otherCards=new ArrayList<Integer>(otherCards);
		Collections.sort(myCards,new Comparator<Integer>(){
            public int compare(Integer org, Integer dest ) {
                return org.compareTo(dest);
            }
        });
		
		Collections.sort(otherCards,new Comparator<Integer>(){
            public int compare(Integer org, Integer dest ) {
                return org.compareTo(dest);
            }
        });
		
		 Map<String,Object> myCardRuleMap=getFuGuiCardRule(myCards);
		 
		NnCardRuleEnum myCardRule=(NnCardRuleEnum) myCardRuleMap.get("cardRuleEnum"); 
		int myBigCard= (Integer)myCardRuleMap.get("card");
		
		Map<String,Object> otherCardMap=getFuGuiCardRule(otherCards);
		
		NnCardRuleEnum otherCardRule=(NnCardRuleEnum) otherCardMap.get("cardRuleEnum");
		int otherBigCard=(Integer)otherCardMap.get("card");
		if(myCardRule==otherCardRule){
			if(myBigCard==otherBigCard){
				return getMaxCard(myCards)>getMaxCard(otherCards);
			}
			return myBigCard>otherBigCard;
		}else{
			return myCardRule.getCode()>otherCardRule.getCode();
		}
	}
	/**
	 * 比较大小
	 * @param myCards
	 * @param otherCards
	 * @return
	 */
	private static boolean compareCard(List<Integer> myCards,Map<String,Object> otherMap,Integer otherMaxCard){
		
		NnCardRuleEnum myCardRule=(NnCardRuleEnum) getCardRule(myCards).get("cardRuleEnum"); 
		int myBigCard= (Integer)getCardRule(myCards).get("card");
		
		NnCardRuleEnum otherCardRule=(NnCardRuleEnum) otherMap.get("cardRuleEnum");
		int otherBigCard=(Integer)otherMap.get("card");
		if(myCardRule==otherCardRule){
			if(myBigCard==otherBigCard){
				return getMaxCard(myCards)>otherMaxCard;
			}
			return myBigCard>otherBigCard;
		}else{
			return myCardRule.getCode()>otherCardRule.getCode();
		}
	}
	

	/***
	 * 判断是否为5小牛
	 * 【五张牌均小于5且和不大于10。例如：11223,黑桃>红桃>梅花>方片】
	 * @param list
	 * @return
	 */
	private static int isFiveSmallNiu(List<Integer> list){
		int sum=0;
		
		int curCard=0;
		for(Integer card:list){
			sum+=getNum(card);
			curCard=curCard>card?curCard:card;
		}
		if(sum<10){
			return curCard;
		}
		return 0;
	}
	
	/**
	 * 判断是否欢乐牛
	 * 【规则：5张数值连续且花色相同的牌组成，黑桃>红桃>梅花>方片】
	 * @return
	 */
	private static int isHuanLeNiu(List<Integer> list){
		
		int curCard=0;
		for(Integer card:list){
			
			if(curCard<=0){
				curCard=card;
				continue;
			}
			//判断花色是否相同
			if(getColor(curCard)!=getColor(card)){
				return 0;
			}
			if(card-curCard!=10){
				return 0;
			}
			curCard=card;
		}
		
		return curCard;
	}
	
	/**
	 * 判断是否为五花牛
	 * 【五张牌均为花牌（J、Q、K）。例如：JJQQK，黑桃>红桃>梅花>方片】
	 * @param list
	 * @return
	 */
	private static int isFiveBigNiu(List<Integer> list){
		int curCard=0;
		for(Integer card:list){
			if(getNum(card)<NnCardNumEnum._11.getCode()){
				return 0;
			}
			curCard=curCard>card?curCard:card;
		}
		return curCard;
	}
	
	/**
	 * 判断是否有炸牛
	 * 【五张牌中有四张牌大小相同。例如：48888】
	 * @param list
	 * @return
	 */
	private static int isFourZha(List<Integer> list){
		
		List<Integer> numList=new ArrayList<Integer>();
		
		for(Integer card:list){
			Integer num= getNum(card);
			numList.add(num);
		}
	   
		 Set<Integer> uniqueSet = new HashSet<Integer>(numList);  
        for (Integer temp :uniqueSet) {  
        	int size= Collections.frequency(numList, temp);
        	if(4==size){
        		return list.get(2);
        	}
        }
        return 0;
	}
	
	/***
	 * 判断是否葫芦牛
	 * 【3张相同的牌和2张相同的牌组成，黑桃>红桃>梅花>方片】
	 * @param list
	 * @return
	 */
	private static int isHuLuNiu(List<Integer> list){
		
		if(((getNum(list.get(1))-getNum(list.get(0))==0)&&(getNum(list.get(4))-getNum(list.get(2))==0))||
			((getNum(list.get(2))-getNum(list.get(0))==0)&&(getNum(list.get(4))-getNum(list.get(3))==0))){
			return getMaxCard(list);
		}
		return 0;
	}
	
	/**
	 * 判断同花牛
	 * @param list
	 * 【5张相同花色的牌组成，黑桃>红桃>梅花>方片】
	 * @return
	 */
	private static int isTongHuaNiu(List<Integer> list){
		
		int curCard=0;
		for(Integer card:list){
			
			if(curCard<=0){
				curCard=card;
				continue;
			}
			//判断花色是否相同
			if(getColor(curCard)!=getColor(card)){
				return 0;
			}
			curCard=card;
		}
		
		return curCard;
	}
	
	/**
	 * 判断是否是顺子牛
	 * 【5张数值连续的牌组成，黑桃>红桃>梅花>方片】
	 * @param list
	 * @return
	 */
	private static int isShunZiNiu(List<Integer> list){
		
		int curCard=0;
		for(Integer card:list){
			
			if(curCard<=0){
				curCard=card;
				continue;
			}
			if(getNum(card)-getNum(curCard)!=1){
				return 0;
			}
			curCard=card;
		}
		
		return curCard;
		
	}
	/**
	 * 判断是否是牛牛
	 * 【其中三张牌之和为10的倍数，另外两张牌和为10或20。例如：51464，黑桃>红桃>梅花>方片】
	 * @param list
	 * @return
	 */
	private static int isNiuNiu(List<Integer> list){
		int totalSum=getTotalSum(list);
		for(int i=0;i<list.size();i++){
			for(int j=i+1;j<list.size();j++){
				int a=(getNum(list.get(i))>10?10:getNum(list.get(i)))+(getNum(list.get(j))>10?10:getNum(list.get(j)));
				int b=totalSum-a;
				if(a%10==0&&b%10==0){
					return Collections.max(list);
				}
			}
		}
		return 0;
	}
	
	/**
	 * 其中三张牌之和为10的倍数，两位两张牌和为2~19，不含10。例如：57867
	 * @param list
	 * @return
	 */
	private static int isNiuDan(List<Integer> list){
			int totalSum=getTotalSum(list);
			for(int i=0;i<list.size();i++){
				for(int j=i+1;j<list.size();j++){
					int a=(getNum(list.get(i))>10?10:getNum(list.get(i)))+(getNum(list.get(j))>10?10:getNum(list.get(j)));
					int b=totalSum-a;
					if(b%10==0){
						return a%10;
					}
				}
			}
			return 0;
	}
	
	private static int getTotalSum(List<Integer> list){
		int sum=0;
		for(Integer card:list){
			sum+=getNum(card)>10?10:getNum(card);
		}
		return sum;
	}
	
	
	private static Integer getNum(Integer card){
		return Integer.parseInt(card.toString().substring(0, card.toString().length()-1));
	}
	/**
	 * 获取花色
	 * @param card
	 * @return
	 */
	private static Integer getColor(Integer card){
		return card%10%10;
	}
	
	/**
	 * 获取最大牌
	 * @param list
	 * @return
	 */
	private static Integer getMaxCard(List<Integer> list){
		return Collections.max(list);
	}
	
	/**
	 * 获取牌类型和最大值
	 * @param list
	 * @return
	 */
	private static Map<String,Object> getCardRule(List<Integer> list){
		Map<String,Object> map=new HashMap<String,Object>();
		int curCard=0;
		if((curCard=isFiveSmallNiu(list))>0){
			map.put("cardRuleEnum", NnCardRuleEnum.FIVE_SMALL_NIU);
			map.put("card", curCard);
		}else if((curCard=isFiveBigNiu(list))>0){
			map.put("cardRuleEnum", NnCardRuleEnum.FIVE_BIG_NIU);
			map.put("card", curCard);
		}else if((curCard=isFourZha(list))>0){
			map.put("cardRuleEnum", NnCardRuleEnum.FOUR_ZHA);
			map.put("card", curCard);
		}else if((curCard=isNiuNiu(list))>0){
			map.put("cardRuleEnum", NnCardRuleEnum.NIU_NIU);
			map.put("card", curCard);
		}else if((curCard=isNiuDan(list))>0){
			map.put("cardRuleEnum", NnCardRuleEnum.X_NIU);
			map.put("card", curCard);
		}else{
			map.put("cardRuleEnum", NnCardRuleEnum.NONE_NIU);
			map.put("card", getMaxCard(list));
		}
		return map;
	}
	
	/**
	 * 获取牌类型和最大值
	 * @param list
	 * @return
	 */
	private static Map<String,Object> getFuGuiCardRule(List<Integer> list){
		Map<String,Object> map=new HashMap<String,Object>();
		int curCard=0;
		if((curCard=isFiveSmallNiu(list))>0){
			map.put("cardRuleEnum", NnCardRuleEnum.FIVE_SMALL_NIU);
			map.put("card", curCard);
		}else if((curCard=isHuanLeNiu(list))>0){
			map.put("cardRuleEnum", NnCardRuleEnum.HUAN_LE_NIU);
			map.put("card", curCard);
		}else if((curCard=isFiveBigNiu(list))>0){
			map.put("cardRuleEnum", NnCardRuleEnum.FIVE_BIG_NIU);
			map.put("card", curCard);
		}else if((curCard=isFourZha(list))>0){
			map.put("cardRuleEnum", NnCardRuleEnum.FOUR_ZHA);
			map.put("card", curCard);
		}else if((curCard=isHuLuNiu(list))>0){
			map.put("cardRuleEnum", NnCardRuleEnum.HU_LU_NIU);
			map.put("card", curCard);
		}else if((curCard=isTongHuaNiu(list))>0){
			map.put("cardRuleEnum", NnCardRuleEnum.TONG_HUA_NIU);
			map.put("card", curCard);
		}else if((curCard=isShunZiNiu(list))>0){
			map.put("cardRuleEnum", NnCardRuleEnum.SHUN_ZI_NIU);
			map.put("card", curCard);
		}else if((curCard=isNiuNiu(list))>0){
			map.put("cardRuleEnum", NnCardRuleEnum.NIU_NIU);
			map.put("card", curCard);
		}else if((curCard=isNiuDan(list))>0){
			map.put("cardRuleEnum", NnCardRuleEnum.X_NIU);
			map.put("card", curCard);
		}else{
			map.put("cardRuleEnum", NnCardRuleEnum.NONE_NIU);
			map.put("card", getMaxCard(list));
		}
		return map;
	}
	/**
	 * 获取卡类型
	 * @param list
	 * @return
	 */
	public static Integer getCardType(List<Integer> list){
		
		Integer cardType=0;
		Map<String,Object> map=getCardRule(list);
		NnCardRuleEnum nnCardRuleEnum=(NnCardRuleEnum)map.get("cardRuleEnum");
		Integer maxNum=(Integer)map.get("card");
		if(NnCardRuleEnum.FIVE_SMALL_NIU==nnCardRuleEnum){
			return NnCardTypeTransEnum.FIVE_SMALL_NIU.getOrgType();
		}else if(NnCardRuleEnum.FIVE_BIG_NIU==nnCardRuleEnum){
			return NnCardTypeTransEnum.FIVE_BIG_NIU.getOrgType();
		}else if(NnCardRuleEnum.FOUR_ZHA==nnCardRuleEnum){
			return NnCardTypeTransEnum.FOUR_ZHA.getOrgType();
		}else if(NnCardRuleEnum.NIU_NIU==nnCardRuleEnum){
			return NnCardTypeTransEnum.NIU_NIU.getOrgType();
		}else if(NnCardRuleEnum.X_NIU==nnCardRuleEnum){
			cardType=maxNum;
		}else if(NnCardRuleEnum.NONE_NIU==nnCardRuleEnum){
			cardType=0;
		}else{
			cardType=0;
		}
		return cardType;
		
	}
	
	
	/**
	 * 获取卡类型
	 * @param list
	 * @return
	 */
	public static Integer getFuGuiCardType(List<Integer> list){
		list=new ArrayList<Integer>(list);
		Collections.sort(list,new Comparator<Integer>(){
            public int compare(Integer org, Integer dest ) {
                return org.compareTo(dest);
            }
        });
		
		Integer cardType=0;
		Map<String,Object> map=getFuGuiCardRule(list);
		NnCardRuleEnum nnCardRuleEnum=(NnCardRuleEnum)map.get("cardRuleEnum");
		Integer maxNum=(Integer)map.get("card");
		if(NnCardRuleEnum.FIVE_SMALL_NIU==nnCardRuleEnum){
			return NnCardTypeTransEnum.FIVE_SMALL_NIU.getOrgType();
		}else if(NnCardRuleEnum.HUAN_LE_NIU==nnCardRuleEnum){
			return NnCardTypeTransEnum.HUAN_LE_NIU.getOrgType();
		}else if(NnCardRuleEnum.FIVE_BIG_NIU==nnCardRuleEnum){
			return NnCardTypeTransEnum.FIVE_BIG_NIU.getOrgType();
		}else if(NnCardRuleEnum.FOUR_ZHA==nnCardRuleEnum){
			return NnCardTypeTransEnum.FOUR_ZHA.getOrgType();
		}else if(NnCardRuleEnum.HU_LU_NIU==nnCardRuleEnum){
			return NnCardTypeTransEnum.HU_LU_NIU.getOrgType();
		}else if(NnCardRuleEnum.TONG_HUA_NIU==nnCardRuleEnum){
			return NnCardTypeTransEnum.TONG_HUA_NIU.getOrgType();
		}else if(NnCardRuleEnum.SHUN_ZI_NIU==nnCardRuleEnum){
			return NnCardTypeTransEnum.SHUN_ZI_NIU.getOrgType();
		}else if(NnCardRuleEnum.NIU_NIU==nnCardRuleEnum){
			return NnCardTypeTransEnum.NIU_NIU.getOrgType();
		}else if(NnCardRuleEnum.X_NIU==nnCardRuleEnum){
			cardType=maxNum;
		}else if(NnCardRuleEnum.NONE_NIU==nnCardRuleEnum){
			cardType=0;
		}else{
			cardType=0;
		}
		return cardType;
		
	}
	
	/**
	 * 获取牛牛cards
	 * 【其中三张牌之和为10的倍数，另外两张牌和为10或20。例如：51464，黑桃>红桃>梅花>方片】
	 * @param list
	 * @return
	 */
	private static List<Integer> getNiuNiuCards(List<Integer> list){
		int totalSum=getTotalSum(list);
		for(int i=0;i<list.size();i++){
			for(int j=i+1;j<list.size();j++){
				int a=(getNum(list.get(i))>10?10:getNum(list.get(i)))+(getNum(list.get(j))>10?10:getNum(list.get(j)));
				int b=totalSum-a;
				if(a%10==0&&b%10==0){
					List<Integer> cards=new ArrayList<Integer>();
					cards.addAll(list);
					cards.remove(list.get(i));
					cards.remove(list.get(j));
					Collections.sort(cards,new Comparator<Integer>(){
			            public int compare(Integer org, Integer dest ) {
			                return org.compareTo(dest);
			            }
			        });
					return cards;
				}
			}
		}
		return null;
	}
	
	/**
	 * 获取牛单cards
	 * 其中三张牌之和为10的倍数，两位两张牌和为2~19，不含10。例如：57867
	 * @param list
	 * @return
	 */
	public static List<Integer> getNiuDanCards(List<Integer> list){
			int totalSum=getTotalSum(list);
			for(int i=0;i<list.size();i++){
				for(int j=i+1;j<list.size();j++){
					
					int a=(getNum(list.get(i))>10?10:getNum(list.get(i)))+(getNum(list.get(j))>10?10:getNum(list.get(j)));
					int b=totalSum-a;
					if(b%10==0){
						List<Integer> cards=new ArrayList<Integer>();
						cards.addAll(list);
						cards.remove(list.get(i));
						cards.remove(list.get(j));
						Collections.sort(cards,new Comparator<Integer>(){
				            public int compare(Integer org, Integer dest ) {
				                return org.compareTo(dest);
				            }
				        });
						return cards;
					}
				}
			}
			return null;
	}
	
	/**
	 * 排序
	 * [
	 * 排序规则
	 * 1.牛炸，炸弹在前面
	 * 2.牛牛和牛单，牛排在前面
	 * 3.顺序排序
	 * ]
	 * @param list
	 * @return
	 */
	public static List<Integer> sortCards(List<Integer> list){
		Collections.sort(list,new Comparator<Integer>(){
            public int compare(Integer org, Integer dest ) {
                return org.compareTo(dest);
            }
        });
		
		Map<String,Object> map=getCardRule(list);
		NnCardRuleEnum nnCardRuleEnum=(NnCardRuleEnum)map.get("cardRuleEnum");	
		
		if(NnCardRuleEnum.FOUR_ZHA==nnCardRuleEnum){
			Collections.sort(list,new Comparator<Integer>(){
	            public int compare(Integer org, Integer dest ) {
	                return getNum(dest)!=getNum(org)?1:-1;
	            }
	        });
		}else if(NnCardRuleEnum.NIU_NIU==nnCardRuleEnum||
				NnCardRuleEnum.X_NIU==nnCardRuleEnum){
			List<Integer> nList= getNiuCards(list);
			list.removeAll(nList);
			nList.addAll(list);
			return nList;
		}
		
		return list;
	}
	
	
	private static boolean isWin(int rate){
		 long random=(long)(Math.random()*WIN_RATE_BASE);
		 long num=(long)(rate*WIN_RATE_BASE/100);
		 if(random>0&&random<=num){
			return true;
			 
		 }else{
			return false;
		 }
		 
	}
	
	private static List<Integer> getWinNewCards(List<Integer> winCards,List<Integer> allCards){
		Map<String,Object> map=getCardRule(winCards);
		//去除五小牛,欢乐牛,五花牛,炸牛
		if(NnCardRuleEnum.FIVE_SMALL_NIU==map.get("cardRuleEnum")
				||NnCardRuleEnum.HUAN_LE_NIU==map.get("cardRuleEnum")
				||NnCardRuleEnum.FIVE_BIG_NIU==map.get("cardRuleEnum")
				||NnCardRuleEnum.FOUR_ZHA==map.get("cardRuleEnum")
				){
			return null;
		} 
		
		List<Integer> myCards=new ArrayList<Integer>();
		//获取三张牛的牌
		father:
		for(int i=0;i<allCards.size();i++){
			
			for(int j=i+1;j<allCards.size();j++){
				
				for(int m=j+1;m<allCards.size();m++){
					
					int card=(getNum(allCards.get(i))>10?10:getNum(allCards.get(i)))
							+(getNum(allCards.get(j))>10?10:getNum(allCards.get(j)))
							+(getNum(allCards.get(m))>10?10:getNum(allCards.get(m)));
					
					if(card%10==0){
						
						myCards.add(allCards.get(i));
						myCards.add(allCards.get(j));
						myCards.add(allCards.get(m));
						break father;
					}
					
					
				}
				
			}
		 
		}
		
		allCards.remove(myCards.get(0));
		allCards.remove(myCards.get(1));
		allCards.remove(myCards.get(2));
		
	
		lableA:
		for(int i=0;i<allCards.size();i++){
			
			for(int j=i+1;j<allCards.size();j++){
				
				List<Integer> curCards=new ArrayList<Integer>(myCards);
				curCards.add(allCards.get(i));
				curCards.add(allCards.get(j));
				
				boolean flag=compareCard(curCards, new ArrayList<Integer>(winCards));
				
				if(flag){
					myCards=curCards;
					break lableA;
				}
					
				
			}
			
		}
		
		if(myCards.size()==5){
			return myCards;
		}
		
		return null ;
	}
	
	
	

//	private static List<Integer> getSameWinNewCards(List<Integer> winCards,List<Integer> allCards,Integer sameCard){
//		Map<String,Object> map=getCardRule(winCards);
//		//去除五小牛,欢乐牛,五花牛,炸牛
//		if(NnCardRuleEnum.FIVE_SMALL_NIU==map.get("cardRuleEnum")
//				||NnCardRuleEnum.HUAN_LE_NIU==map.get("cardRuleEnum")
//				||NnCardRuleEnum.FIVE_BIG_NIU==map.get("cardRuleEnum")
//				||NnCardRuleEnum.FOUR_ZHA==map.get("cardRuleEnum")
//				){
//			return null;
//		} 
//		
//		List<Integer> myCards=new ArrayList<Integer>();
//		//获取三张牛的牌
//		father:
//		for(int i=0;i<allCards.size();i++){
//			
//			for(int j=i+1;j<allCards.size();j++){
//				
//				for(int m=j+1;m<allCards.size();m++){
//					
//					int card=(getNum(allCards.get(i))>10?10:getNum(allCards.get(i)))
//							+(getNum(allCards.get(j))>10?10:getNum(allCards.get(j)))
//							+(getNum(allCards.get(m))>10?10:getNum(allCards.get(m)));
//					
//					if(card%10==0){
//						
//						myCards.add(allCards.get(i));
//						myCards.add(allCards.get(j));
//						myCards.add(allCards.get(m));
//						break father;
//					}
//					
//					
//				}
//				
//			}
//		 
//		}
//		
//		allCards.remove(myCards.get(0));
//		allCards.remove(myCards.get(1));
//		allCards.remove(myCards.get(2));
//		
//	
//		for(int i=0;i<allCards.size();i++){
//				
//				List<Integer> curCards=new ArrayList<Integer>(myCards);
//				curCards.add(allCards.get(i));
//				curCards.add(sameCard);
//				
//				boolean flag=compareCard(curCards, new ArrayList<Integer>(winCards));
//				
//				if(flag){
//					myCards=curCards;
//					break;
//				}
//					
//			
//		}
//		
//		if(myCards.size()==5){
//			return myCards;
//		}
//		
//		return null ;
//	}
	private static List<Integer> getSameWinNewCards(List<Integer> winCards,List<Integer> allCards,Integer sameCard){
		Map<String,Object> map=getCardRule(winCards);
		//去除五小牛,欢乐牛,五花牛,炸牛
		if(NnCardRuleEnum.FIVE_SMALL_NIU==map.get("cardRuleEnum")
				||NnCardRuleEnum.HUAN_LE_NIU==map.get("cardRuleEnum")
				||NnCardRuleEnum.FIVE_BIG_NIU==map.get("cardRuleEnum")
				||NnCardRuleEnum.FOUR_ZHA==map.get("cardRuleEnum")
				){
			return null;
		} 
		
		 List<Integer> myCards=null;
		//获取三张牛的牌
		father:
		for(int i=0;i<allCards.size();i++){
			
			for(int j=i+1;j<allCards.size();j++){
				
				for(int m=j+1;m<allCards.size();m++){
					
					for(int n=m+1;n<allCards.size();n++){
						
					    myCards=new ArrayList<Integer>();
						myCards.add(allCards.get(i));
						myCards.add(allCards.get(j));
						myCards.add(allCards.get(m));
						myCards.add(allCards.get(n));
						myCards.add(sameCard);
						boolean flag=compareCard(myCards, new ArrayList<Integer>(winCards));
						
						if(flag){
							
							break father;
						}
						
						
					}
					
					
					
				}
				
			}
		 
		}
		if(myCards.size()==5){
			return myCards;
		}
		
		return null ;
	}
	
	
	
	private static List<Integer> getLoseNewCards(List<Integer> loseCards,List<Integer> allCards){
		Map<String,Object> otherMap=getCardRule(loseCards);
		Integer maxCard=getMaxCard(loseCards);
		
		List<Integer> myCards=new ArrayList<Integer>();
		father:
		for(int i=0;i<allCards.size();i++){
			
			for(int j=i+1;j<allCards.size();j++){
				
				for(int m=j+1;m<allCards.size();m++){
					
					for(int n=m+1;n<allCards.size();n++){
						
						for(int y=n+1;y<allCards.size();y++){
							
							List<Integer> curCards=new ArrayList<Integer>(myCards);
							curCards.add(allCards.get(i));
							curCards.add(allCards.get(j));
							curCards.add(allCards.get(m));
							curCards.add(allCards.get(n));
							curCards.add(allCards.get(y));
							
							boolean flag=compareCard(curCards, otherMap, maxCard);
							
							if(!flag){
								myCards=curCards;
								break father;
							}
							
						}
						
					}
					
				}
				
			}
		 
		}
		
		
		if(myCards.size()==5){
			return myCards;
		}
		System.out.println("暂无牌型.....................");
		return null ;
	}
	private static List<Integer> getSameLoseNewCards(List<Integer> loseCards,List<Integer> allCards,Integer sameCard){
		Map<String,Object> otherMap=getCardRule(loseCards);
		Integer maxCard=getMaxCard(loseCards);
		
		List<Integer> myCards=new ArrayList<Integer>();
		father:
		for(int i=0;i<allCards.size();i++){
			
			for(int j=i+1;j<allCards.size();j++){
				
				for(int m=j+1;m<allCards.size();m++){
					
					for(int n=m+1;n<allCards.size();n++){
						
							
							List<Integer> curCards=new ArrayList<Integer>(myCards);
							curCards.add(allCards.get(i));
							curCards.add(allCards.get(j));
							curCards.add(allCards.get(m));
							curCards.add(allCards.get(n));
							curCards.add(sameCard);
							
							boolean flag=compareCard(curCards, otherMap, maxCard);
							
							if(!flag){
								myCards=curCards;
								break father;
							}
							
						
					}
					
				}
				
			}
		 
		}
		
		
		if(myCards.size()==5){
			return myCards;
		}
		System.out.println("暂无牌型.....................");
		return null ;
	}

	public static void main(String[] args) {
		
		Map<String, List<Integer>> mapCard=getSimpleCard("1000","1001","1003");
		System.out.println(JSONObject.toJSONString(mapCard));
		
		System.out.println(mapCard.get("1000").subList(0, 4));
	}
	
}
