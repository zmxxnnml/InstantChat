package com.zhiyi.im.client;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.zhiyi.im.common.DateUtil;
import com.zhiyi.im.common.thread.NamedThreadFactory;
import com.zhiyi.im.config.InstantChatConfig;

/**
 * Manager of online clients.
 */
public class OnlineClientMgr {
	private static final Logger logger = Logger.getLogger(OnlineClientMgr.class);
	
	// key: clientId
	private static ConcurrentHashMap<String, OnlineClient> clients =
			new ConcurrentHashMap<String, OnlineClient>();
	
	private final static String SESSION_KEY = "session_key";

	public static final AttributeKey<String> attr = AttributeKey.valueOf(SESSION_KEY);
	
	private Lock addClientLock = new ReentrantLock();
	
	private ScheduledExecutorService scheduledThreadPool;
	
	private OnlineClientMgr() {}
	
	public void scan() {
		scheduledThreadPool = new ScheduledThreadPoolExecutor(
				1, new NamedThreadFactory("instantchat-scheduled", false));
		scheduledThreadPool.scheduleWithFixedDelay(new ScanAllSessionRunner(),
				InstantChatConfig.getInstance().getSessionScanInterval(),
				InstantChatConfig.getInstance().getSessionScanInterval(),
				TimeUnit.SECONDS);
	}
	
	private static class ClientMgrHolder {
		public static final OnlineClientMgr instance= new OnlineClientMgr();
	}
	
	public static OnlineClientMgr getInstance() {
		return ClientMgrHolder.instance;
	}
	
	public void addClient(String clientId, Channel channel) {
		OnlineClient client = null;
		addClientLock.lock();
		try {
			if (clients.containsKey(clientId)) {
				OnlineClient preClient = clients.get(clientId);
				if (preClient != null) {
					logger.warn("kill pre client: " + clientId);
					removeClient(preClient.getChannel());
				}
			}
			channel.attr(attr).set(clientId);
			client = new OnlineClient(clientId, channel);
			clients.put(clientId, client);
		} finally {
			addClientLock.unlock();
		}
	}
	
	public void removeClient(Channel channel) {
		if (channel == null) {
			logger.warn("channel is null");
			return;
		}

		Attribute<String> attribute = channel.attr(attr);
		String key = attribute.get();
		if (key == null) {
			logger.warn("bad connection: " + channel.toString());
			return;
		}

		if (clients.containsKey(key)) {
			OnlineClient client = clients.get(key);
			client.stop();
			clients.remove(key);
		} else {
			logger.warn("remove device,but channel don't exist!");
		}
	}
	
	public void removeClient(String clientId) {
		if (clients.containsKey(clientId)) {
			OnlineClient client = clients.get(clientId);
			client.stop();
			clients.remove(clientId);
		} else {
			logger.warn("remove device,but channel don't exist!");
		}
	}

	public void destory() {
		for (OnlineClient client : clients.values()) {
			client.stop();
		}
		clients.clear();
	}

	public void touch(Channel channel) {
		String key = channel.attr(attr).get();

		if (clients.containsKey(key)) {
			OnlineClient session = clients.get(key);
			session.visit();
		} else {
			logger.warn("touch channel ,but the channel doesn't exist!");
		}
	}
	
	public void touch(String clientId) {
		if (clients.containsKey(clientId)) {
			OnlineClient session = clients.get(clientId);
			session.visit();
		} else {
			logger.warn("touch channel ,but the channel doesn't exist!");
		}
	}

	public Collection<OnlineClient> getSessions() {
		return clients.values();
	}

	public OnlineClient getClient(String clientId) {
		return clients.get(clientId);
	}

	public OnlineClient getClient(Channel channel) {
		String key = channel.attr(attr).get();
		return clients.get(key);
	}

	public long getClientCount() {
		return clients.values().size();
	}

	private class ScanAllSessionRunner implements Runnable {

		@Override
		public void run() {
			logger.error("online clients: " + clients.size());
			
			long currentTime = DateUtil.getCurrentMillisTimeUTC();
			for(Entry<String, OnlineClient> entry : clients.entrySet()) {
				OnlineClient client = entry.getValue();
				if (client.getLastOperationTimeStamp().get()
						+ InstantChatConfig.getInstance().getConnectionUnactiveDeadline() * 1000
						< currentTime) {
					logger.warn("Connection is unactive for quite a while: " + client.getRemoteSocketAddress());
					removeClient(entry.getKey());
				}
			}
			
		}
	}
	
}
