package com.zhiyi.InstantChat.client;

import io.netty.channel.Channel;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.zhiyi.InstantChat.base.DateUtil;

/**
 * Manager to manage the "to be authorized" clients.
 * 
 * Used to void connection attack.
 *
 */
public class PendingClientMgr {
	// If the client can't be authorized in #{CONNECTION_UNAUTHORIZED_PENDDING_TIME} seconds
	// after connecting server, we will shutdown the channel.
	private static final Integer CONNECTION_UNAUTHORIZED_PENDDING_TIME = 30;  // 30seconds
	
	private static final Logger logger = Logger.getLogger(PendingClientMgr.class);
	
	private static ConcurrentHashMap<Integer, PendingClient> clients =
			new ConcurrentHashMap<Integer, PendingClient>();
	
	private PendingClientMgr() {}
	
	private static class UnauthorizedClientMgrHolder {
		public static final PendingClientMgr instance= new PendingClientMgr();
	}
	
	public static PendingClientMgr getInstance() {
		return UnauthorizedClientMgrHolder.instance;
	}
	
	public PendingClient getClient(int channelHashCode) {
		return clients.get(channelHashCode);
	}
	
	public void addClient(PendingClient client) {
		clients.put(client.getChannel().hashCode(), client);
	}
	
	public void removeAndCloseClient(int channelHashCode) {
		PendingClient client = getClient(channelHashCode);
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
		logger.info("pending clients: " + clients.size());
		
		long currentTime = DateUtil.getCurrentSecTimeUTC();
		
		for(Entry<Integer, PendingClient> entry : clients.entrySet()) {
			PendingClient client = entry.getValue();
			if (client.getConnectedTime() + CONNECTION_UNAUTHORIZED_PENDDING_TIME < currentTime
					|| client.getFailAuthorized()) {
				removeAndCloseClient(entry.getKey());
			}
		}
	}
}
