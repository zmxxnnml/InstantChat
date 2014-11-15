package com.zhiyi.im.client;

import io.netty.channel.Channel;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.zhiyi.im.base.DateUtil;
import com.zhiyi.im.base.NamedThreadFactory;
import com.zhiyi.im.config.InstantChatConfig;

/**
 * Manager to manage the "to be authorized" clients.
 * 
 * Used to void connection attack.
 *
 * TODO: Refactor the code.
 */
public class PendingClientMgr {

	private static final Logger logger = Logger.getLogger(PendingClientMgr.class);
	
	private static ConcurrentHashMap<Integer, PendingClient> clients =
			new ConcurrentHashMap<Integer, PendingClient>();
	
	private ScheduledExecutorService scheduledThreadPool;
	
	private PendingClientMgr() {}
	
	public void scan() {
		scheduledThreadPool = new ScheduledThreadPoolExecutor(
				1, new NamedThreadFactory("instantchat-scheduled", false));
		scheduledThreadPool.scheduleWithFixedDelay(new ScanAllSessionRunner(),
				InstantChatConfig.getInstance().getSessionScanInterval(),
				InstantChatConfig.getInstance().getSessionScanInterval(),
				TimeUnit.SECONDS);
	}
	
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
		// TODO: change the key alg.
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

	private class ScanAllSessionRunner implements Runnable {

		@Override
		public void run() {
			logger.error("pending clients: " + clients.size());

			long currentTime = DateUtil.getCurrentMillisTimeUTC();
			for (Entry<Integer, PendingClient> entry : clients.entrySet()) {
				PendingClient client = entry.getValue();
				if (client.getConnectedTime() + InstantChatConfig.getInstance().
						getConnectionUnauthorizedDeadline() *1000 < currentTime
						|| client.getFailAuthorized()) {
					removeAndCloseClient(entry.getKey());
				}

			}
		}
	}
}
