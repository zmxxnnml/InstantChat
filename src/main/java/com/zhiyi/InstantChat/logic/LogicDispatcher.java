package com.zhiyi.InstantChat.logic;

import io.netty.channel.Channel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogicDispatcher {
	
	private static final int MAX_THREAD_NUM = 50;
	
	private static ExecutorService executorService =
			Executors.newFixedThreadPool(MAX_THREAD_NUM);
	
	public static void submit(Channel channel, Object msgObject) {
		// TODO
	}
}
