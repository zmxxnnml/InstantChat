package com.zhiyi.InstantChat.logic;

import io.netty.channel.Channel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.zhiyi.InstantChat.protobuf.ChatPkg.PkgC2S;

public class LogicDispatcher {
	
	// TODO: put it into configuration
	private static final int MAX_THREAD_NUM = 50;
	
	private static ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREAD_NUM);
	
	public static void submit(Channel channel, PkgC2S pkgC2S) {
		
		BaseHandler handler = LogicHandlerMgr.getHandler(pkgC2S.getType());
		if (handler == null) {
			return;
		}
		
		handler.setChannel(channel);
		handler.setPkgC2S(pkgC2S);
		
		executorService.submit(handler);
	}
}
