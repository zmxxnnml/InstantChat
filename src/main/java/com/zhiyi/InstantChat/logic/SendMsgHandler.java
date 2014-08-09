package com.zhiyi.InstantChat.logic;

import com.zhiyi.InstantChat.base.DateUtil;
import com.zhiyi.InstantChat.protobuf.ChatPkg.ChatMessage;

public class SendMsgHandler extends BaseHandler {
	@Override
	public void run() {
		ChatMessage chatMsg = pkgC2S.getMessage();
		if (chatMsg == null) {
			// TODO: LOG ERROR
			return;
		}
		
		if (!chatMsg.hasFromDeviceId() && ! chatMsg.hasFromUid()) {
			// TODO: LOG ERROR
			return;
		}
		
		if (!chatMsg.hasToDeviceId() && !chatMsg.hasToUid()) {
			// TODO: LOG ERROR
			return;
		}
		
		long userSendTime = -1;
		if (chatMsg.hasUserSendTime()) {
			userSendTime = chatMsg.getUserSendTime();
		}
		chatMsg.toBuilder().setUserSendTime(adjustClientTime(userSendTime));
		
		// TODO: interact with MongoDB
		// 1. Get next-seq(touid/todeviceid) meanwhile increase it in Mongodb 
		//    (Make the process to be a transcation)
		// 2. Update seq field of chat message
		// 3. Store into mongodb
		// 4. Send notification to push server.
	}
	
	private long adjustClientTime(long clientTime) {
		long serverTime = DateUtil.getCurrentSecTimeUTC();
		if (clientTime + 600 < serverTime || clientTime - 600 > serverTime) {
			return serverTime;
		}
		return clientTime;
	}
}
