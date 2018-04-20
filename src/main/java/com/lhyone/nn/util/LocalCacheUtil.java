package com.lhyone.nn.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 本地缓存工具类
 * @author admin
 *
 */
@SuppressWarnings("unchecked")
public class LocalCacheUtil {

	public final static Map<String,Object> map=new HashMap<String,Object>();
	
	/**
	 * 设置map值,加锁处理
	 * @param key
	 * @param filed
	 * @param obj
	 */
	public synchronized static void hset(String key,String filed,Object obj){
		map.put(key+filed, obj);
	}
	
	
	/**
	 * 获取map值
	 * @param key
	 * @param field
	 * @return
	 */
	public  static <T> T hget(String key,String field){
		return (T) map.get(key+field);
	}
	
	/**
	 * 判断是否存在该map
	 * @param key
	 * @param field
	 * @return
	 */
	public static boolean hexist(String key,String field){
		Object obj=map.get(key+field);
		if(null!=obj){
			return true;
		}
		return false;
	}
	
	/**
	 * 删除map
	 * @param key
	 * @param fields
	 * @return
	 */
	public static int hdel(String key,String... fields){
		int i=0;
		for(String field:fields){
			Object obj=map.remove(key+field);
			if(null!=obj){
				i++;
			}
		}
		return i;
	}
	
}
