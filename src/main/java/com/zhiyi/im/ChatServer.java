package com.zhiyi.im;

import org.apache.log4j.Logger;

import com.zhiyi.im.client.OnlineClientMgr;
import com.zhiyi.im.client.PendingClientMgr;
import com.zhiyi.im.config.InstantChatConfig;
import com.zhiyi.im.metaq.MsgConsumer;
import com.zhiyi.im.metaq.MsgSender;
import com.zhiyi.im.protobuf.ChatPkg.PkgC2S;
import com.zhiyi.im.storage.DbServiceImpl;

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

public class ChatServer {
	private int port;
	
	private static final Logger logger = Logger.getLogger(ChatServer.class);
	
	public ChatServer(int port) {
		this.port = port;
	}
	
	public void run() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                	 // Decoders
                	ch.pipeline().addLast(
                			"frameDecoder", new ProtobufVarint32FrameDecoder());
                	ch.pipeline().addLast(
                			"protobufDecoder", new ProtobufDecoder(PkgC2S.getDefaultInstance()));

                	 // Encoders
                	ch.pipeline().addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
                	ch.pipeline().addLast("protobufEncoder", new ProtobufEncoder());
                	
                	// In handler
                    ch.pipeline().addLast(new MsgInHandler());
                    
                    // Out handler
                    ch.pipeline().addLast(new MsgOutHandler());
                }
            })
            .option(ChannelOption.SO_BACKLOG, 128)
            .childOption(ChannelOption.SO_KEEPALIVE, true);
			
			ChannelFuture f = b.bind(port).sync();
			logger.error("server is running...");
			f.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}
	
	public static void main(String[] args) throws Exception {
		logger.error("Instant server is starting...");
		
		// load configuration.
		InstantChatConfig.getInstance().preload();
		
		int port;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		} else {
			port = InstantChatConfig.getInstance().getServerPort();  
		}
		
		// Connect to database
		DbServiceImpl.getInstance().init();
		logger.error("Connect to database success!");
		
		// Add cronjob to scan dead connection
		OnlineClientMgr.getInstance().scan();
		logger.error("Dead connection scanner is running...");
		
		// Add cronjob to scan pending(unauthorized) connection
		PendingClientMgr.getInstance().scan();
		logger.error("pending connection scanner is running...");
		
		// Init rocketmq sender.
		MsgSender msgSender = MsgSender.getInstance();
		msgSender.setNameServer("");		// TODO: fill name server.
		msgSender.init();
		
		// Init rocketmq  consumer.
		MsgConsumer msgConsumer = MsgConsumer.getInstance();
		msgConsumer.setNameServer("");  // TODO: fill name server.
		msgConsumer.init();
		
		new ChatServer(port).run();
	}
	
}