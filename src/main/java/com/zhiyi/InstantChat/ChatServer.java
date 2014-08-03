package com.zhiyi.InstantChat;

import com.zhiyi.InstantChat.protobuf.ChatPkg.PkgC2S;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;

public class ChatServer {
	// TODO: make it to be configured
	private static final int DEFAULT_PORT = 21423;
	
	private int port;
	
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
                	ch.pipeline().addLast("frameEncoder", new LengthFieldPrepender(4));
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
			port = DEFAULT_PORT;  
		}
		
		new ChatServer(port).run();
		
		// TODO: add cronjob to scan dead connection
		
		// TODO: add cronjob to scan pending(unauthorized) connection
		
	}
	
}