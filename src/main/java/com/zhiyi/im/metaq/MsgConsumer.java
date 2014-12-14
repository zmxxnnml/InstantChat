package com.zhiyi.im.metaq;

import java.io.Serializable;
import java.util.List;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.zhiyi.im.client.OnlineClient;
import com.zhiyi.im.client.OnlineClientMgr;
import com.zhiyi.im.protobuf.ChatPkg.MsgNotifyS2C;
import com.zhiyi.im.protobuf.ChatPkg.PkgS2C;
import com.zhiyi.im.protobuf.ChatPkg.PkgS2C.PkgType;

public class MsgConsumer extends BaseConsumer {
	
	private MsgConsumer() {}
	
	private static class MsgConsumerHolder {
		public static final MsgConsumer instance= new MsgConsumer();
	}
	
	public static MsgConsumer getInstance() {
		return MsgConsumerHolder.instance;
	}
	
	@Override
	public TopicEnum getTopicEnum() {
		return TopicEnum.NOTIFYASYNCQUEUE;
	}

	@Override
	public void doLogErrorConsumeMessage(MsgObj msgObj) {
        // ignore
	}

	@Override
	public ConsumeConcurrentlyStatus doConsumeMessage(MsgObj msgObj) {
		List<Serializable> msgs = msgObj.getMsgList();
		for (Serializable msg : msgs) {
			MsgNotify notify = (MsgNotify)msg;
			String toDeviceId = notify.getToDeviceId();
			OnlineClient onlineClient = OnlineClientMgr.getInstance().getClient(toDeviceId);
			if (onlineClient != null) {
				PkgS2C.Builder pkgS2CBuilder = PkgS2C.newBuilder();
				pkgS2CBuilder.setType(PkgType.MSG_NOTIFY);
				
				MsgNotifyS2C.Builder msgNotifyS2C = MsgNotifyS2C.newBuilder();
				msgNotifyS2C.setFromDeviceId(notify.getFromDeviceId());
				msgNotifyS2C.setFromUid(notify.getFromUid());
				
				pkgS2CBuilder.setMsgNotify(msgNotifyS2C.build());
				
				onlineClient.getChannel().writeAndFlush(pkgS2CBuilder.build());
			}
		}
		
		return null;
	}

}
