package com.zhiyi.InstantChat;

import io.netty.channel.Channel;

public class Client {
	
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