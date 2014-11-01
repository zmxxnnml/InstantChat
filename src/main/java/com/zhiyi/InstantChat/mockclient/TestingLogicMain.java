package com.zhiyi.InstantChat.mockclient;

import org.apache.log4j.Logger;

public class TestingLogicMain {
	
	private static final Logger logger = Logger.getLogger(TestingLogicMain.class);
	
	private static final String MOCK_DEVICE_ID = genMockDeviceId("1");
	
	private static final Long MOCK_USER_ID = genMockUserId(1L);
	
	private static final String MOCK_SEC_TOKEN = "mock_sec_token";
	
	private static final String MOCK_TO_DEVICE_ID = genMockDeviceId("2");
	
	private static final Long MOCK_TO_USER_ID = genMockUserId(2L);
	
	private static MockClient mockClient;
	
	private static String genMockDeviceId(String random) {
		String mockDeviceId = "mock_device_id_" + System.currentTimeMillis() + "_" + random;
		return mockDeviceId;
	}
	
	private static Long genMockUserId(Long random) {
		Long mockUserId = System.currentTimeMillis() + random;
		return mockUserId;
	}
	
	public static void main(String[] argv) throws Exception {
		mockClient = new MockClient(MOCK_USER_ID, MOCK_DEVICE_ID, MOCK_SEC_TOKEN);

		// Send reg pkg.
		Thread sendRegThread = new Thread(new Runnable() {

			public void run() {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						logger.error(e);
					}
					mockClient.sendRegPkg();
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
					mockClient.sendHeartBeat();
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
					mockClient.sendMessage(MOCK_TO_USER_ID, MOCK_TO_DEVICE_ID);
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
					mockClient.pullMessage();
				}
			}
			
		});
		pullMessageThread.start();
		logger.info("pull message thread running...");
		
		// Because it block the thread, so put mockClient.run() at the end.
		mockClient.run();  // connect the server.
	}

}
