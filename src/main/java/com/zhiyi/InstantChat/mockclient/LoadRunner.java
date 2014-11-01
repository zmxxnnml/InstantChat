package com.zhiyi.InstantChat.mockclient;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Performace testing.
 * 
 * testing items:
 * 1. how many connections can the server support?
 * 2. how many actived clients can be handled by server?
 * 
 * how to simulate an actived client actions?
 * Client:
 * 1.  heart beat
 * 2.  send message to somebody
 * 3. Get message from somebody
 */
public class LoadRunner {

	private static final Logger logger = Logger.getLogger(LoadRunner.class);
	
	private static final Integer DEFAULT_CLIENTS_NUM = 100;
	
	private List<ClientIdentifier> clientIdentifiers;
	
	public LoadRunner(int clientNum) {
		clientIdentifiers = generateClientIdentifiers(clientNum);
	}
	
	public LoadRunner() {
		clientIdentifiers = generateClientIdentifiers(DEFAULT_CLIENTS_NUM);
	}
	
	public void testIdleClientsPerf() {
		
		for (int i = 0; i < clientIdentifiers.size(); ++ i) {
			final MockClient mockClient = new MockClient(
					clientIdentifiers.get(i).getUserId(), clientIdentifiers.get(i).getDeviceId(), "");
			
			// Reg client.
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
			
			// Heartbeat every 10s.
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
			
			mockClient.run();  // Put it at the end.
		}
		
	}

	public void testActiveClientsPerf() {
		for (int i = 0; i < clientIdentifiers.size(); ++ i) {
			final int k = i;
			
			final MockClient mockClient = new MockClient(
					clientIdentifiers.get(i).getUserId(), clientIdentifiers.get(i).getDeviceId(), "");
			
			// Reg client.
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
			
			// Heartbeat every 10s.
			Thread heartBeatThread = new Thread(new Runnable() {

				public void run() {
					try {
						Thread.sleep(8000);
					} catch (InterruptedException e1) {
						logger.error(e1);
					}
					
					while (true) {
						try {
							Thread.sleep(10000);  // 10s
						} catch (InterruptedException e) {
							logger.error(e);
						}
						mockClient.sendHeartBeat();
					}
				}
				
			});
			heartBeatThread.start();
			logger.info("heartbeat thread running...");
			
			//  0<->1  2<->3  4<->5 .... send and receive message
			Thread sendMessageThread = new Thread(new Runnable() {

				public void run() {
					
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e1) {
						logger.error(e1);
					}
					
					while (true) {
						try {
							Thread.sleep(3000);  //3s
						} catch (InterruptedException e) {
							logger.error(e);
						}
						
						long toUserId = -1;
						String toDeviceId = "d_undefined";
						if ((k & 1) == 0) {
							if (k+1 < clientIdentifiers.size()) {
								toUserId = clientIdentifiers.get(k+1).getUserId();
							}
						} else {
							if (k-1 >= 0) {
								toUserId = clientIdentifiers.get(k-1).getUserId();
							}
						}
						
						mockClient.sendMessage(toUserId, toDeviceId);
					}
					
				}
				
			});
			sendMessageThread.start();
			
			// Pull messages every 3s.
			Thread pullMessageThread = new Thread(new Runnable() {

				public void run() {
					
					try {
						Thread.sleep(15000);
					} catch (InterruptedException e1) {
						logger.error(e1);
					}
					
					while (true) {
						try {
							Thread.sleep(3000);  // 3s
						} catch (InterruptedException e) {
							logger.error(e);
						}
						mockClient.pullMessage();
					}
				}
				
			});
			pullMessageThread.start();
			logger.info("pull message thread running...");
			
			mockClient.run();  // Put it at the end.
		}
		
	}
	
	private List<ClientIdentifier> generateClientIdentifiers(int clientNum) {
		List<ClientIdentifier> clients = new ArrayList<ClientIdentifier>();
		for (int i = 0; i < clientNum; ++i) {
			long userId = 10001 + clientNum;
			String deviceId = "d_" + userId;
			ClientIdentifier clientIdentifier = new ClientIdentifier(userId, deviceId);
			clients.add(clientIdentifier);
		}
		
		return clients;
	}
	
	static class ClientIdentifier {
		private long userId;
		private String deviceId;
		
		public ClientIdentifier() {}
		
		public ClientIdentifier(long userId, String deviceId) {
			this.userId = userId;
			this.deviceId = deviceId;
		}
		
		public long getUserId() {
			return userId;
		}
		
		public void setUserId(long userId) {
			this.userId = userId;
		}
		
		public String getDeviceId() {
			return deviceId;
		}
		
		public void setDeviceId(String deviceId) {
			this.deviceId = deviceId;
		}
	}
	
}
