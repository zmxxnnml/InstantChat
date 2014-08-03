package com.zhiyi.InstantChat.client;

import io.netty.channel.Channel;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.zhiyi.InstantChat.base.DateUtil;
import com.zhiyi.InstantChat.exception.ClientNotExistingException;

/**
 * Manager of online clients.
 * 
 * TODO: It's to be discussed about what to used to be as client identifier.
 * currently we use device id as client identifier. Later we may use user ID.
 *
 */
public class OnlineClientMgr {
	// TODO: put it into configuration
	private static final Integer CONNECTION_DEADLINE = 10;
	
	private static ConcurrentHashMap<String, AppClient> clients =
			new ConcurrentHashMap<String, AppClient>();
	
	private OnlineClientMgr() {}
	
	private static class ClientMgrHolder {
		public static final OnlineClientMgr instance= new OnlineClientMgr();
	}
	
	public static OnlineClientMgr getInstance() {
		return ClientMgrHolder.instance;
	}
	
	public AppClient getClient(String deviceId) {
		AppClient client = clients.get(deviceId);
		return client;
	}
	
	public void addClient(String deviceId, AppClient client) {
		AppClient existingClient = getClient(deviceId);
		if (existingClient != null) {
			Channel channel = client.getChannel();
			channel.flush();
			channel.close();
			clients.remove(deviceId);
		}
		clients.put(deviceId, client);
	}
	
	public void removeClient(String deviceId) {
		AppClient client = getClient(deviceId);
		if (client != null) {
			Channel channel = client.getChannel();
			channel.flush();
			channel.close();
			clients.remove(deviceId);
		}
	}
	
	public void removeClient(Channel channel) {
		// TODO: it's very slow to scan the whole hashmap one by one and compare a big object
		// TODO: To clarify how much memory space the "channel" cost?
		// TODO: Make the search faster
		for(Entry<String, AppClient> entry : clients.entrySet()) {
			AppClient client = entry.getValue();
			if (channel.equals(client.getChannel())) {
				removeClient(entry.getKey());
				break;
			}
		}
	}
	
	public void refreshClientHeartBeat(String deviceId, long lastHeatBeatTime)
			throws ClientNotExistingException {
		AppClient client = clients.get(deviceId);
		if (client == null) {
			throw new ClientNotExistingException();
		}
		
		client.setLastHeartBeatTime(lastHeatBeatTime);
	}
	
	public void checkConnProcess() {
		long currentTime = DateUtil.getCurrentSecTimeUTC();
		
		for(Entry<String, AppClient> entry : clients.entrySet()) {
			AppClient client = entry.getValue();
			if (client.getLastHeartBeatTime() + CONNECTION_DEADLINE < currentTime) {
				removeClient(entry.getKey());
			}
		}
	}
}