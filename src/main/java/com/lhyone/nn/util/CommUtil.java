package com.lhyone.nn.util;


import com.lhyone.crypt.AuthToken;

import java.util.Random;

/**
 * Created by Think on 2017/8/30.
 */
public class CommUtil {
    private static Random random = new Random();
    public static Long getRandomUid(String players){
        try {
            String[] uids = players.split(",");
            int index = getRandomOrder(1, 3) - 1;
            return Long.parseLong(uids[index]);
        }catch (Exception ce){
            ce.printStackTrace();
            return null;
        }
    }
    private static int getRandomOrder(int min ,int max){
        int s = random.nextInt(max)%(max-min+1) + min;
        return s;
    }
    public static boolean authcheck(String tokenString,Long uid){
    	return true;
//        if (tokenString == null) {
//            return false;
//        }
//        AuthToken authToken = null;
//        try {
//            authToken = AuthToken.parse(tokenString);
//        } catch (Exception e) {
//            return false;
//        }
//        if(uid!=authToken.userId){
//            return false;
//        }
//        if (AuthToken.isActive(authToken)) {
//            return true;
//        }
//        return  false;
    }
}
