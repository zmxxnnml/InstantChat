package com.zhiyi.im.logic;

import com.zhiyi.im.protobuf.ChatPkg.PkgC2S;

import io.netty.channel.Channel;

public class BaseHandler implements Runnable {

	protected Channel channel;
	
	protected PkgC2S pkgC2S;

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public Object getPkgC2S() {
		return pkgC2S;
	}

	public void setPkgC2S(PkgC2S pkgC2S) {
		this.pkgC2S = pkgC2S;
	}

	@Override
	public void run() {}
}