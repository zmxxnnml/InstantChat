package com.zhiyi.im.client;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import com.zhiyi.im.common.DateUtil;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Wrapper used to represent a client.
 */
public class OnlineClient {

	// uid or device id or other identifier.
	private String clientId;
	
	private Channel channel;

	private final AtomicLong lastOperationTimeStamp = new AtomicLong(0);
	
	public OnlineClient() {}
	
	public OnlineClient(String clientId, Channel channel) {
		this.clientId = clientId;
		this.channel = channel;
		this.lastOperationTimeStamp.set(DateUtil.getCurrentMillisTimeUTC());
	}
	
	public String getClientId() {
		return clientId;
	}
	
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public Channel getChannel() {
		return channel;
	}
	
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
	public boolean isActive() {
		return (this.channel != null && this.channel.isActive());
	}
	
	public InetSocketAddress getRemoteSocketAddress() {
		return ((NioSocketChannel) channel).remoteAddress();
	}
	
	public AtomicLong getLastOperationTimeStamp() {
		return lastOperationTimeStamp;
	}

	public void visit() {
		this.lastOperationTimeStamp.set(DateUtil.getCurrentMillisTimeUTC());
	}
	
	public void stop() {
		if (channel != null || channel.isActive()) {
			channel.close();
		}
	}
}