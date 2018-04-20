package com.lhyone.nn.logic;

import java.util.concurrent.TimeUnit;

import com.lhyone.nn.enums.NnTimeTaskEnum;
import com.lhyone.nn.logic.handler.MyTimerTask;
import com.lhyone.nn.logic.handler.NnDealHandler;
import com.lhyone.nn.logic.handler.ServerManager;
import com.lhyone.nn.pb.NnBean;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4J2LoggerFactory;

/**
 * Created by Think on 2017/8/30.
 */
public class NnServer {
    public static void main(String[] args)  throws Exception{
    	InternalLoggerFactory.setDefaultFactory(new Log4J2LoggerFactory());
    	ServerManager.executorTask.scheduleWithFixedDelay(new MyTimerTask(null,NnTimeTaskEnum.LISTEN_TIME.getCode(),0), 0, 200, TimeUnit.MILLISECONDS);
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            System.out.println("服务已启动");
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .childHandler(new ChannelInitializer<SocketChannel>(){
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                        	
                            socketChannel.pipeline().addLast(new ProtobufVarint32FrameDecoder());//
                            socketChannel.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                            socketChannel.pipeline().addLast(new ProtobufDecoder(NnBean.ReqMsg.getDefaultInstance()));
                            socketChannel.pipeline().addLast(new ProtobufEncoder());
                            socketChannel.pipeline().addLast(new ReadTimeoutHandler(600));
//                          socketChannel.pipeline().addLast(new WriteTimeoutHandler(600));  
                            socketChannel.pipeline().addLast(new NnDealHandler());
//                          socketChannel.pipeline().addLast(new MyServerHanlder());
//                            socketChannel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        }
                    });
            ChannelFuture f = b.bind(9004).sync();
            f.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    
}
