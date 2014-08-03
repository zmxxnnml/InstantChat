package com.zhiyi.InstantChat.client;

import io.netty.channel.Channel;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.zhiyi.InstantChat.base.DateUtil;
/**
 * Manager to manage the "to be authorized" clients.
 * 
 * Used to void connection attack.
 *
 */
public class UnauthorizedClientMgr {
	// If the client can't be authorized in #{CONNECTION_UNAUTHORIZED_PENDDING_TIME} seconds
	// after connecting server, we will shutdown the channel.
	private static final Integer CONNECTION_UNAUTHORIZED_PENDDING_TIME = 10;
	
	private static ConcurrentHashMap<Integer, UnauthorizedAppClient> clients =
			new ConcurrentHashMap<Integer, UnauthorizedAppClient>();
	
	private UnauthorizedClientMgr() {}
	
	private static class UnauthorizedClientMgrHolder {
		public static final UnauthorizedClientMgr instance= new UnauthorizedClientMgr();
	}
	
	public static UnauthorizedClientMgr getInstance() {
		return UnauthorizedClientMgrHolder.instance;
	}
	
	public UnauthorizedAppClient getClient(int channelHashCode) {
		return clients.get(channelHashCode);
	}
	
	public void addClient(UnauthorizedAppClient client) {
		clients.put(client.getChannel().hashCode(), client);
	}
	
	public void removeAndCloseClient(int channelHashCode) {
		UnauthorizedAppClient client = getClient(channelHashCode);
		if (client != null) {
			Channel channel = client.getChannel();
			channel.flush();
			channel.close();
			clients.remove(channelHashCode);
		}
	}
	
	public void removeClient(int channelHashCode) {
		clients.remove(channelHashCode);
	}
	
	public void checkConnProcess() {
		long currentTime = DateUtil.getCurrentSecTimeUTC();
		
		for(Entry<Integer, UnauthorizedAppClient> entry : clients.entrySet()) {
			UnauthorizedAppClient client = entry.getValue();
			if (client.getConnectedTime() + CONNECTION_UNAUTHORIZED_PENDDING_TIME < currentTime
					|| client.getFailAuthorized()) {
				removeAndCloseClient(entry.getKey());
			}
		}
	}
}
