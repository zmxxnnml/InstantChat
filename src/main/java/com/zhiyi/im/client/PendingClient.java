package com.zhiyi.im.client;

import io.netty.channel.Channel;

public class PendingClient {
	private Channel channel;
	
	private long connectedTime;
	
	// true if the client has invoked authorization process but failed.
	private boolean failAuthorized;
	
	public PendingClient() {
		failAuthorized = false;
	}
	
	public Channel getChannel() {
		return channel;
	}
	
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
	public long getConnectedTime() {
		return connectedTime;
	}
	
	public void setConnectedTime(long connectedTime) {
		this.connectedTime = connectedTime;
	}
	
	public boolean getFailAuthorized() {
		return failAuthorized;
	}
	
	public void setFailAuthorized(boolean failAuthorized) {
		this.failAuthorized = failAuthorized;
	}
}
