package com.zhiyi.InstantChat.logic;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.zhiyi.InstantChat.protobuf.ChatPkg.ChatMessage;
import com.zhiyi.InstantChat.protobuf.ChatPkg.PkgS2C;
import com.zhiyi.InstantChat.protobuf.ChatPkg.PullMessageS2C;
import com.zhiyi.InstantChat.protobuf.ChatPkg.PullReqC2S;
import com.zhiyi.InstantChat.protobuf.ChatPkg.PkgS2C.PkgType;
import com.zhiyi.InstantChat.protobuf.ChatPkg.RetCode;
import com.zhiyi.InstantChat.storage.DbService;
import com.zhiyi.InstantChat.storage.MongoDbServiceImpl;

public class PullMsgHandler extends BaseHandler {
	private static final Logger logger = Logger.getLogger(PullMsgHandler.class);
	
	@Override
	public void run() {
		PkgS2C.Builder pkgS2CBuilder = PkgS2C.newBuilder();
		pkgS2CBuilder.setType(PkgType.MESSAGE);
		
		PullMessageS2C.Builder pullMessageS2C = PullMessageS2C.newBuilder();
		if (!pkgC2S.hasPullReq()) {
			// TODO: log ERROR
			pullMessageS2C.setCode(RetCode.ILLEGAL_REQUEST);
			pkgS2CBuilder.setPullMsgAck(pullMessageS2C.build());
			logger.info("send:\n" + pkgS2CBuilder.build().toString());
			channel.writeAndFlush(pkgS2CBuilder.build());
			return;
		}
		
		PullReqC2S pullReqC2S = pkgC2S.getPullReq();
		if (!pullReqC2S.hasDeviceId()) {
			pullMessageS2C.setCode(RetCode.ILLEGAL_REQUEST);
			pkgS2CBuilder.setPullMsgAck(pullMessageS2C.build());
			logger.info("send:\n" + pkgS2CBuilder.build().toString());
			channel.writeAndFlush(pkgS2CBuilder.build());
			return;
		}

		DbService dbService = MongoDbServiceImpl.getInstance();
		
		List<ChatMessage> messages = new ArrayList<ChatMessage>();
		if (pullReqC2S.hasReqStartSeq() && pullReqC2S.hasReqEndSeq()) {
			// Get messages by start seq and end seq.
			List<ChatMessage> messagesFromBb  = dbService.getChatMessageBySeq(
					pullReqC2S.getDeviceId(), pullReqC2S.getReqStartSeq(), pullReqC2S.getReqEndSeq());
			messages.addAll(messagesFromBb);
		} else if (pullReqC2S.hasStartTimestamp() && pullReqC2S.hasNum() && pullReqC2S.hasGreater()) {
			// Get messages by start timestamp and num.
			List<ChatMessage> messagesFromBb  = dbService.getDeviceChatMessagesByDate(
					pullReqC2S.getDeviceId(), pullReqC2S.getStartTimestamp(), pullReqC2S.getNum(), pullReqC2S.getGreater());
			messages.addAll(messagesFromBb);
		}
		
		pullMessageS2C.setCode(RetCode.SUCCESS);
		pullMessageS2C.addAllMessages(messages);
		pkgS2CBuilder.setPullMsgAck(pullMessageS2C);
		logger.info("send:\n" + pkgS2CBuilder.build().toString());
		channel.writeAndFlush(pkgS2CBuilder.build());
		
		// Update ack seq.
		if (pullReqC2S.hasDeviceId() && pullReqC2S.hasAckReq()) {
			dbService.updateAckSeq(pullReqC2S.getDeviceId(), pullReqC2S.getAckReq());
		}
		
	}
}
