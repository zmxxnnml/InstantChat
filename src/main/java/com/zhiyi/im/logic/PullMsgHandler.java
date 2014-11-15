package com.zhiyi.im.logic;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.zhiyi.im.protobuf.ChatPkg.ChatMessage;
import com.zhiyi.im.protobuf.ChatPkg.PkgS2C;
import com.zhiyi.im.protobuf.ChatPkg.PullMessageS2C;
import com.zhiyi.im.protobuf.ChatPkg.PullReqC2S;
import com.zhiyi.im.protobuf.ChatPkg.RetCode;
import com.zhiyi.im.protobuf.ChatPkg.PkgS2C.PkgType;
import com.zhiyi.im.storage.DbService;
import com.zhiyi.im.storage.DbServiceImpl;

/**
 * 
 * The request has following 5 parameters:[ for detail, see PullReqC2S in ChatPkg.proto]
 * optional int64 req_start_seq = 5;
 * optional int64 req_end_seq = 6;
 * optional int64 start_timestamp = 7;
 * optional bool  greater = 8;
 * optional int64 num = 9;
 * 
 *  So app-clients have many different ways to pull messages by filling different parameters above.
 * 
 * Currently we only implement the following 3 ways to pull messages:
 * 1. Pull messages which message seq is between #{req_start_seq} and #{req_end_seq}
 * 2. Pull #{num} messages which timestamp is smaller than #{start_timestamp}
 * 3. Pull #{num} messages which timestamp is bigger than #{start_timestamp}
 * 
 * Later, we can add more according to app-client need
 * 
 */
public class PullMsgHandler extends BaseHandler {
	private static final Logger logger = Logger.getLogger(PullMsgHandler.class);
	
	@Override
	public void run() {
		PkgS2C.Builder pkgS2CBuilder = PkgS2C.newBuilder();
		pkgS2CBuilder.setType(PkgType.PULL_RESP);
		
		PullMessageS2C.Builder pullMessageS2C = PullMessageS2C.newBuilder();
		PullReqC2S pullReqC2S = pkgC2S.getPullReq();
		if (pullReqC2S == null || (!pullReqC2S.hasDeviceId() && !pullReqC2S.hasUid())) {
			logger.warn("Illegal pull message req packet: " + pkgC2S.toString());
			pullMessageS2C.setCode(RetCode.ILLEGAL_REQUEST);
			pkgS2CBuilder.setPullMsgAck(pullMessageS2C.build());
			logger.info("send:\n" + pkgS2CBuilder.build().toString());
			channel.writeAndFlush(pkgS2CBuilder.build());
			return;
		}
		
		DbService dbService = DbServiceImpl.getInstance();
		
		// TODO:xxxxxxxxxxxxxxxxxxxxxxxxxxx
		// Update ack seq.
		if (pullReqC2S.hasDeviceId() && pullReqC2S.hasAckReq()) {
			dbService.updateAckSeq(pullReqC2S.getDeviceId(), pullReqC2S.getAckReq());
		} else if (pullReqC2S.hasUid() && pullReqC2S.hasAckReq()) {
			dbService.updateAckSeq(pullReqC2S.getUid(), pullReqC2S.getAckReq());
		}
		
		// Get messages from db and send to target client.
		List<ChatMessage> messages = new ArrayList<ChatMessage>();
		if (pullReqC2S.hasReqStartSeq() && pullReqC2S.hasReqEndSeq()) {
			// Get messages by start seq and end seq.
			List<ChatMessage> messagesFromBb  = dbService.getChatMessageBySeq(
					pullReqC2S.getDeviceId(), pullReqC2S.getReqStartSeq(), pullReqC2S.getReqEndSeq());
			messages.addAll(messagesFromBb);
		} else if (pullReqC2S.hasStartTimestamp() && pullReqC2S.hasNum() && pullReqC2S.hasGreater()) {
			// Get messages by start timestamp and num.
			List<ChatMessage> messagesFromBb  = dbService.getDeviceChatMessagesByTimestamp(
					pullReqC2S.getDeviceId(), pullReqC2S.getStartTimestamp(),
					pullReqC2S.getNum(), pullReqC2S.getGreater());
			messages.addAll(messagesFromBb);
		}
		
		pullMessageS2C.setCode(RetCode.SUCCESS);
		pullMessageS2C.addAllMessages(messages);
		pkgS2CBuilder.setPullMsgAck(pullMessageS2C);
		logger.info("Pull messages :\n" + pkgS2CBuilder.build().toString());
		channel.writeAndFlush(pkgS2CBuilder.build());
	}
}
