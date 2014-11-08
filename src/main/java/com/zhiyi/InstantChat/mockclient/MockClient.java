package com.zhiyi.InstantChat.mockclient;

import org.apache.log4j.Logger;

import com.google.protobuf.ByteString;
import com.zhiyi.InstantChat.config.InstantChatConfig;
import com.zhiyi.InstantChat.protobuf.ChatPkg.ChatMessage;
import com.zhiyi.InstantChat.protobuf.ChatPkg.HeartBeatC2S;
import com.zhiyi.InstantChat.protobuf.ChatPkg.PkgC2S;
import com.zhiyi.InstantChat.protobuf.ChatPkg.PkgS2C;
import com.zhiyi.InstantChat.protobuf.ChatPkg.PullReqC2S;
import com.zhiyi.InstantChat.protobuf.ChatPkg.RegC2S;
import com.zhiyi.InstantChat.protobuf.ChatPkg.ChatMessage.MessageType;

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
	
	private static final String SERVER_ADDR =  "127.0.0.1";
	
	private ChannelFuture future;
	
	private long userId;
	
	private String deviceId;
	
	private String secToken;
	
	public MockClient() {}
	
	public MockClient(long userId, String deviceId, String secToken) {
		this.userId = userId;
		this.deviceId = deviceId;
		this.secToken = secToken;
	}

	private void sendMessage(PkgC2S pkgc2s) {
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
	
	public void sendRegPkg() {
		PkgC2S.Builder pkgBuilder = PkgC2S.newBuilder();
		pkgBuilder.setType(PkgC2S.PkgType.REG);
		RegC2S.Builder regBuilder = RegC2S.newBuilder();
		regBuilder.setDeviceId(deviceId);
		regBuilder.setUid(userId);
		regBuilder.setSecToken(secToken);
		
		pkgBuilder.setReg(regBuilder.build());
		sendMessage(pkgBuilder.build());
	}
	
	public void sendHeartBeat() {
		PkgC2S.Builder pkgBuilder = PkgC2S.newBuilder();
		pkgBuilder.setType(PkgC2S.PkgType.HEART_BEAT);
		HeartBeatC2S.Builder heartbeatBuilder = HeartBeatC2S.newBuilder();
		heartbeatBuilder.setDeviceId(deviceId);
		heartbeatBuilder.setUid(userId);
		heartbeatBuilder.setSendTime(System.currentTimeMillis() / 1000);
		
		pkgBuilder.setHeartBeat(heartbeatBuilder.build());
		sendMessage(pkgBuilder.build());
	}
	
	public void sendMessage(long toUserId, String toDeviceId) {
		PkgC2S.Builder pkgBuilder = PkgC2S.newBuilder();
		pkgBuilder.setType(PkgC2S.PkgType.MESSAGE);
		ChatMessage.Builder messageBuilder = ChatMessage.newBuilder();
		messageBuilder.setFromDeviceId(deviceId);
		messageBuilder.setFromUid(userId);
		messageBuilder.setToDeviceId(toDeviceId);
		messageBuilder.setToUid(toUserId);
		messageBuilder.setType(ChatMessage.MessageType.TEXT);
		Long currentTime = System.currentTimeMillis();
		ByteString bs = ByteString.copyFrom(currentTime.toString().getBytes());
		messageBuilder.setData(bs);
		messageBuilder.setDataLen(currentTime.toString().length());
		messageBuilder.setUserSendTime(currentTime);
		messageBuilder.setType(MessageType.TEXT);
		pkgBuilder.setMessage(messageBuilder.build());
		sendMessage(pkgBuilder.build());
	}
	
	public void pullMessage() {
		PkgC2S.Builder pkgBuilder = PkgC2S.newBuilder();
		pkgBuilder.setType(PkgC2S.PkgType.PULL_REQ);
		
		PullReqC2S.Builder pullBuilder = PullReqC2S.newBuilder();
		pullBuilder.setDeviceId(deviceId);
		pullBuilder.setReqStartSeq(1);
		pullBuilder.setReqEndSeq(10);
		
		pkgBuilder.setPullReq(pullBuilder.build());
		sendMessage(pkgBuilder.build());
	}

	public void run() {
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

			future = b.connect(SERVER_ADDR, InstantChatConfig.getInstance().getServerPort()).sync();
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