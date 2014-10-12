package com.zhiyi.InstantChat.logic;

import org.apache.log4j.Logger;

import com.zhiyi.InstantChat.base.DateUtil;
import com.zhiyi.InstantChat.protobuf.ChatPkg.ChatMessage;
import com.zhiyi.InstantChat.storage.DbService;
import com.zhiyi.InstantChat.storage.MongoDbServiceImpl;

public class SendMsgHandler extends BaseHandler {
	private static final Logger logger = Logger.getLogger(SendMsgHandler.class);
	
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
		
		// Save message into database.
		DbService db = MongoDbServiceImpl.getInstance();
		db.saveChatMessage(chatMsg);
		logger.info("save chatmsg into mongodb success!");
	}
	
	private long adjustClientTime(long clientTime) {
		long serverTime = DateUtil.getCurrentSecTimeUTC();
		if (clientTime + 600 < serverTime || clientTime - 600 > serverTime) {
			return serverTime;
		}
		return clientTime;
	}
}
