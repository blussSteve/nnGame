package com.lhyone.nn.logic.handler;

import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.lhyone.nn.dao.NnManagerDao;
import com.lhyone.nn.entity.NnRoom;

public class DataInit {
	private final static ThreadPoolExecutor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

	
	public static void dataInit(int port){
		List<NnRoom> list=NnManagerDao.instance().queryNnRoom(port);
		
		for(final NnRoom bean:list){
			//判断是否有庄，如果没有庄则设置庄家，如果有则增加相应的定时器
			executor.execute(new Runnable() {
				
				@Override
				public void run() {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
//					HunNnManager.sysDataInit(bean.getRoomNo());
					
				}
			});
			
		}
		
	}
}
