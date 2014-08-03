package com.zhiyi.InstantChat.client;

import io.netty.channel.Channel;

/**
 * Wrapper used to represent a client.
 * 
 * TODO: It's to be discussed about what to used to be as client identifier.
 * currently we use device id as client identifier. Later we may use user ID.
 *
 */
public class AppClient {

	private String deviceId;
	
	private Channel channel;
	
	private long lastHeartBeatTime;
	
	public String getDeviceId() {
		return deviceId;
	}
	
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	public Channel getChannel() {
		return channel;
	}
	
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
	public long getLastHeartBeatTime() {
		return lastHeartBeatTime;
	}
	
	public void setLastHeartBeatTime(long heartBeatTime) {
		this.lastHeartBeatTime = heartBeatTime;
	}
	
}