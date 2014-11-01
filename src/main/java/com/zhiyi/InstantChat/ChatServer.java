package com.zhiyi.InstantChat;

import org.apache.log4j.Logger;

import com.zhiyi.InstantChat.client.OnlineClientMgr;
import com.zhiyi.InstantChat.client.PendingClientMgr;
import com.zhiyi.InstantChat.config.InstantChatConfig;
import com.zhiyi.InstantChat.protobuf.ChatPkg.PkgC2S;

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
			logger.info("server is running...");
			f.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}
	
	public static void main(String[] args) throws Exception {
		int port;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		} else {
			port = InstantChatConfig.getInstance().getServerPort();  
		}
		
		// Add cronjob to scan dead connection
		OnlineClientMgr.getInstance().scan();
		logger.info("Dead connection scanner is running...");
		
		// Add cronjob to scan pending(unauthorized) connection
		PendingClientMgr.getInstance().scan();
		logger.info("pending connection scanner is running...");
		
		new ChatServer(port).run();
	}
	
}