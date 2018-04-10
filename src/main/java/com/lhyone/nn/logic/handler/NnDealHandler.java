package com.lhyone.nn.logic.handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lhyone.nn.pb.NnBean;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by Think on 2017/8/15.
 */
public class NnDealHandler extends ChannelInboundHandlerAdapter {
    // 业务逻辑线程池(业务逻辑最好跟netty io线程分开处理，线程切换虽会带来一定的性能损耗，但可以防止业务逻辑阻塞io线程)
    private final static ExecutorService workerThreadService = newBlockingExecutorsUseCallerRun(Runtime.getRuntime().availableProcessors() * 5);
    private static Logger logger=LogManager.getLogger(NnDealHandler.class);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    	NnBean.ReqMsg reqMsg = (NnBean.ReqMsg)msg;
//    	logger.info("request ip is {},msg is {}",ctx.channel().remoteAddress(), reqMsg);		
    	ServerManager.initChannel(reqMsg, ctx);
        //加入在线用户
        // 使用自定义业务线程处理复杂的业务逻辑，不会影响netty io线程
        workerThreadService.execute(new NnTask(ctx, reqMsg));
    }
    
    
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    	logger.info("异常连接channelId {}",ctx.channel().id().asLongText());
    	logger.error(cause.getMessage(),cause);
        cause.printStackTrace();
        exit(ctx);
    }
    private void exit(ChannelHandlerContext ctx){
    	ServerManager.delChannel(ctx);
        ctx.close();
    }
    /**
     * 阻塞的ExecutorService
     *
     * @param size
     * @return
     */
    public static ExecutorService newBlockingExecutorsUseCallerRun(int size) {
        return new ThreadPoolExecutor(size, size, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(),
                new RejectedExecutionHandler() {
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        try {
                            executor.getQueue().put(r);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

}
