package com.zhiyi.im.logic;

import org.apache.log4j.Logger;

import com.zhiyi.im.common.DateUtil;
import com.zhiyi.im.config.InstantChatConfig;
import com.zhiyi.im.metaq.MsgNotify;
import com.zhiyi.im.metaq.MsgSender;
import com.zhiyi.im.metaq.TopicEnum;
import com.zhiyi.im.protobuf.ChatPkg.ChatMessage;
import com.zhiyi.im.storage.DbService;
import com.zhiyi.im.storage.DbServiceImpl;
import com.zhiyi.im.storage.StorageException;
import com.zhiyi.im.trans.ApplicationServerTransporter;
import com.zhiyi.im.trans.ApplicationServerTransporterFactory;
import com.zhiyi.im.trans.exception.DeviceNotExistingException;
import com.zhiyi.im.trans.exception.InternalException;
import com.zhiyi.im.trans.exception.UserNotExistingException;

/*
 *  TODO:
 *   In v0.1.0, send ack message to client.
 *  
 *  TODO: 
 *  In v0.2.0, we can adjust architecture: send the message directly to target client immediately.
 *  
 *                    connector              connector             connector
 *                                    \                             |                                   /
 *                                        \                         |                                 /
 *                                                   Message queue
 *                                              
 *    1. all connector connect to message queue
 *    2. A client send message x to client B
 *    3. If B is android client, 
 *         Send notification to message queue and save message into db
 *    4. If B is ios client,
 *         Send notification to message queue and save message into db and send apns a notification.
 *         When B receive the apns notification, B should re-check if the message has be received or not.
 */
public class SendMsgHandler extends BaseHandler {
	private static final Logger logger = Logger.getLogger(SendMsgHandler.class);
	
	@Override
	public void run() {
		ChatMessage chatMsg = pkgC2S.getMessage();
		if (chatMsg == null
				|| (!chatMsg.hasFromDeviceId() && ! chatMsg.hasFromUid())
				|| (!chatMsg.hasToDeviceId() && !chatMsg.hasToUid())) {
			logger.warn("Illegal sendmsg packet: " + pkgC2S.toString());
			return;
		}

		Long userSendTime = -1L;
		if (chatMsg.hasUserSendTime()) {
			userSendTime = chatMsg.getUserSendTime();
			long costTime = DateUtil.getCurrentMillisTimeUTC() - userSendTime;
			logger.info("Time cost of client sending message to server: " + costTime + "ms." + userSendTime + "  " + DateUtil.getCurrentMillisTimeUTC());
		}
		chatMsg.toBuilder().setUserSendTime(DateUtil.getCurrentMillisTimeUTC());
		
		// Save message into database.
		DbService db = DbServiceImpl.getInstance();
		
		try {
			long messageId = db.saveChatMessage(chatMsg);
			logger.info("Save message successfully, messageId:" + messageId);
		} catch (StorageException e) {
			// TODO: do nothing for now.
			return;
		}

		// Put notify msg into rocketmq.
		MsgNotify msgNotify = new MsgNotify();
		if (chatMsg.hasFromDeviceId()) {
			msgNotify.setFromDeviceId(chatMsg.getFromDeviceId());
		}
		if (chatMsg.hasFromUid()) {
			msgNotify.setFromUid(chatMsg.getFromUid());
		}
		if (chatMsg.hasToDeviceId()) {
			msgNotify.setToDeviceId(chatMsg.getToDeviceId());
		}
		if (chatMsg.hasToUid()) {
			msgNotify.setToUid(chatMsg.getToUid());
		}
		MsgSender.getInstance().sendMessage(msgNotify,
				TopicEnum.NOTIFYASYNCQUEUE.getTopic(), TopicEnum.NOTIFYASYNCQUEUE.getTags());
		
		// Send notification to target client only for ios app.
		try {
			ApplicationServerTransporter transporter  =
					ApplicationServerTransporterFactory.getTransporter(
					InstantChatConfig.getInstance().getApplicationServerType());
			 // TODO: talk about what to send later.
			transporter.sendApnsNotificationToClient(
					chatMsg.getToUid(), chatMsg.getToDeviceId(), null);
		} catch (InternalException e) {
			logger.warn("Send notificaiton failed  by internal error: " + e);
		} catch (DeviceNotExistingException e) {
			logger.warn(
					"Send notificaiton failed  by DeviceNotExisting: " + chatMsg.getToDeviceId());
		} catch (UserNotExistingException e) {
			logger.warn("Send notificaiton failed  by UseNotExisting: " + chatMsg.getToUid());
		}
		
	}
	
}
