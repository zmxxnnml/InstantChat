package com.zhiyi.InstantChat.mockclient;

import org.apache.log4j.Logger;

import com.google.protobuf.ByteString;
import com.zhiyi.InstantChat.protobuf.ChatPkg.ChatMessage;
import com.zhiyi.InstantChat.protobuf.ChatPkg.HeartBeatC2S;
import com.zhiyi.InstantChat.protobuf.ChatPkg.PkgC2S;
import com.zhiyi.InstantChat.protobuf.ChatPkg.PullReqC2S;
import com.zhiyi.InstantChat.protobuf.ChatPkg.RegC2S;
import com.zhiyi.InstantChat.protobuf.ChatPkg.ChatMessage.MessageType;

public class TestingMain {
	
	private static final Logger logger = Logger.getLogger(TestingMain.class);
	
	private static final String MOCK_DEVICE_ID = "mock_device_id_1";
	
	private static final Long MOCK_USER_ID = 1L;
	
	private static final String MOCK_SEC_TOKEN = "mock_sec_token";
	
	private static final String MOCK_TO_DEVICE_ID = "mock_device_id_2";
	
	private static final Long MOCK_TO_USER_ID = 2L;
	
	private static MockClient mockClient;
	
	public static void main(String[] argv) throws Exception {
		mockClient = new MockClient();

		// Send heartbeat pkg every 10s.
		Thread sendRegThread = new Thread(new Runnable() {

			public void run() {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						logger.error(e);
					}
					sendRegPkg();
			}
			
		});
		sendRegThread.start();
		logger.info("send reg thread running...");
		
		// Send heartbeat pkg every 10s.
		Thread heartBeatThread = new Thread(new Runnable() {

			public void run() {
				while (true) {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						logger.error(e);
					}
					sendHeartBeat();
				}
			}
			
		});
		heartBeatThread.start();
		logger.info("heartbeat thread running...");
		
		// Send one message every 5s.
		Thread sendMessageThread = new Thread(new Runnable() {

			public void run() {
				while (true) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						logger.error(e);
					}
					sendMessage();
				}
			}
			
		});
		sendMessageThread.start();
		logger.info("send message thread running...");
		
		// Pull messages every 10s.
		Thread pullMessageThread = new Thread(new Runnable() {

			public void run() {
				while (true) {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						logger.error(e);
					}
					pullMessage();
				}
			}
			
		});
		pullMessageThread.start();
		logger.info("pull message thread running...");
		
		mockClient.run();  // connect the server.
	}
	
	private static void sendRegPkg() {
		PkgC2S.Builder pkgBuilder = PkgC2S.newBuilder();
		pkgBuilder.setType(PkgC2S.PkgType.REG);
		RegC2S.Builder regBuilder = RegC2S.newBuilder();
		regBuilder.setDeviceId(MOCK_DEVICE_ID);
		regBuilder.setUid(MOCK_USER_ID);
		regBuilder.setSecToken(MOCK_SEC_TOKEN);
		
		pkgBuilder.setReg(regBuilder.build());
		mockClient.sendMessage(pkgBuilder.build());
	}
	
	private static void sendHeartBeat() {
		PkgC2S.Builder pkgBuilder = PkgC2S.newBuilder();
		pkgBuilder.setType(PkgC2S.PkgType.HEART_BEAT);
		HeartBeatC2S.Builder heartbeatBuilder = HeartBeatC2S.newBuilder();
		heartbeatBuilder.setDeviceId(MOCK_DEVICE_ID);
		heartbeatBuilder.setUid(MOCK_USER_ID);
		heartbeatBuilder.setSendTime(System.currentTimeMillis() / 1000);
		
		pkgBuilder.setHeartBeat(heartbeatBuilder.build());
		mockClient.sendMessage(pkgBuilder.build());
	}
	
	private static void sendMessage() {
		PkgC2S.Builder pkgBuilder = PkgC2S.newBuilder();
		pkgBuilder.setType(PkgC2S.PkgType.MESSAGE);
		ChatMessage.Builder messageBuilder = ChatMessage.newBuilder();
		messageBuilder.setFromDeviceId(MOCK_DEVICE_ID);
		messageBuilder.setFromUid(MOCK_USER_ID);
		messageBuilder.setToDeviceId(MOCK_TO_DEVICE_ID);
		messageBuilder.setToUid(MOCK_TO_USER_ID);
		messageBuilder.setType(ChatMessage.MessageType.TEXT);
		Long currentTime = System.currentTimeMillis();
		ByteString bs = ByteString.copyFrom(currentTime.toString().getBytes());
		messageBuilder.setData(bs);
		messageBuilder.setUserSendTime(currentTime);
		messageBuilder.setType(MessageType.TEXT);
		String text = "hello tester!";
		messageBuilder.setData(ByteString.copyFromUtf8(text));
		messageBuilder.setDataLen(text.length());
		messageBuilder.setUserSendTime(System.currentTimeMillis()/1000);
		pkgBuilder.setMessage(messageBuilder.build());
		mockClient.sendMessage(pkgBuilder.build());
	}
	
	private static void pullMessage() {
		PkgC2S.Builder pkgBuilder = PkgC2S.newBuilder();
		pkgBuilder.setType(PkgC2S.PkgType.PULL_REQ);
		
		PullReqC2S.Builder pullBuilder = PullReqC2S.newBuilder();
		pullBuilder.setDeviceId(MOCK_TO_DEVICE_ID);  // Just for testing.
		pullBuilder.setReqStartSeq(1);
		pullBuilder.setReqEndSeq(10);
		
		pkgBuilder.setPullReq(pullBuilder.build());
		mockClient.sendMessage(pkgBuilder.build());
	}
}
