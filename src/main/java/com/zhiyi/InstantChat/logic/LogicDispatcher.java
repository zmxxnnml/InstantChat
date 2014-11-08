package com.zhiyi.InstantChat.logic;

import io.netty.channel.Channel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.zhiyi.InstantChat.config.InstantChatConfig;
import com.zhiyi.InstantChat.protobuf.ChatPkg.PkgC2S;

public class LogicDispatcher {
	private static Logger logger = Logger.getLogger(LogicDispatcher.class);
	
	private static ExecutorService executorService = Executors.newFixedThreadPool(
			InstantChatConfig.getInstance().getMaxLogicThreadNum());
	
	public static void submit(Channel channel, PkgC2S pkgC2S) {
		
		BaseHandler handler = LogicHandlerMgr.getHandler(pkgC2S.getType());
		if (handler == null) {
			logger.warn("Unknow packet type: " + pkgC2S.getType());
			return;
		}
		
		handler.setChannel(channel);
		handler.setPkgC2S(pkgC2S);
		
		executorService.submit(handler);
	}
}
