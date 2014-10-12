package com.zhiyi.InstantChat.mockclient;

import org.apache.log4j.Logger;

import com.zhiyi.InstantChat.protobuf.ChatPkg.PkgC2S;
import com.zhiyi.InstantChat.protobuf.ChatPkg.PkgS2C;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

public class MockClient implements Runnable {
	private static final Logger logger = Logger.getLogger(MockClient.class);
	
	private ChannelFuture future;
	
	public void sendMessage(PkgC2S pkgc2s) {
		if (future == null  || !future.channel().isActive()) {
			logger.error("Connection is not active.");
			return;
		}
		
		logger.info("send:\n" + pkgc2s.toString());
		
		future.channel().writeAndFlush(pkgc2s);
		future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
            	logger.info("send success!");
            }
        }); 
	}

	public void run() {
		String host = "127.0.0.1";
		int port = 21423;
		
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			Bootstrap b = new Bootstrap();
			b.group(workerGroup);
			b.channel(NioSocketChannel.class);
			b.option(ChannelOption.SO_KEEPALIVE, true);
			b.handler(new ChannelInitializer<SocketChannel>() {

				protected void initChannel(SocketChannel ch) throws Exception {
					// Decoders
					ch.pipeline().addLast("frameDecoder",
							new ProtobufVarint32FrameDecoder());
					ch.pipeline().addLast("protobufDecoder",
							new ProtobufDecoder(PkgS2C.getDefaultInstance()));

					// Encoders
					ch.pipeline().addLast("frameEncoder",
							new ProtobufVarint32LengthFieldPrepender());
					ch.pipeline().addLast("protobufEncoder",
							new ProtobufEncoder());

					ch.pipeline().addLast(new MsgInHandler());
				}

			});

			future = b.connect(host, port).sync();
			if (future != null && future.channel().isActive()) {
				logger.info("connect success!");
			} else {
				logger.error("connect failed!");
			}
			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			workerGroup.shutdownGracefully();
		}
	}

}