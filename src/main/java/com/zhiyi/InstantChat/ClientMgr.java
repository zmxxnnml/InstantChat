package com.zhiyi.InstantChat;

import io.netty.channel.Channel;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.zhiyi.InstantChat.base.DateUtil;
import com.zhiyi.InstantChat.exception.ClientNotExistingException;

public class ClientMgr {
	// TODO: put it into configuration
	private static final Integer CONNECTION_DEADLINE = 10;
	
	private static ConcurrentHashMap<String, Client> clients =
			new ConcurrentHashMap<String, Client>();
	
	private ClientMgr() {}
	
	private static class ClientMgrHolder {
		public static final ClientMgr instance= new ClientMgr();
	}
	
	public static ClientMgr getInstance() {
		return ClientMgrHolder.instance;
	}
	
	public Client getClient(String deviceId) {
		Client client = clients.get(deviceId);
		return client;
	}
	
	public void addClient(String deviceId, Client client) {
		Client existingClient = getClient(deviceId);
		if (existingClient != null) {
			Channel channel = client.getChannel();
			channel.flush();
			channel.close();
			clients.remove(deviceId);
		}
		clients.put(deviceId, client);
	}
	
	public void removeClient(String deviceId) {
		Client client = getClient(deviceId);
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
		for(Entry<String, Client> entry : clients.entrySet()) {
			Client client = entry.getValue();
			if (channel.equals(client.getChannel())) {
				removeClient(entry.getKey());
				break;
			}
		}
	}
	
	public void refreshClientHeartBeat(String deviceId, long lastHeatBeatTime)
			throws ClientNotExistingException {
		Client client = clients.get(deviceId);
		if (client == null) {
			throw new ClientNotExistingException();
		}
		
		client.setLastHeartBeatTime(lastHeatBeatTime);
	}
	
	public void checkConnProcess() {
		long currentTime = DateUtil.getCurrentSecTimeUTC();
		
		for(Entry<String, Client> entry : clients.entrySet()) {
			Client client = entry.getValue();
			if (client.getLastHeartBeatTime() + CONNECTION_DEADLINE < currentTime) {
				removeClient(entry.getKey());
			}
		}
	}
}
