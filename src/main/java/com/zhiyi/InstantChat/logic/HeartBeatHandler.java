package com.zhiyi.InstantChat.logic;

import org.apache.log4j.Logger;

import com.zhiyi.InstantChat.base.DateUtil;
import com.zhiyi.InstantChat.client.ClientIdGenerator;
import com.zhiyi.InstantChat.client.OnlineClientMgr;
import com.zhiyi.InstantChat.protobuf.ChatPkg.HeartBeatC2S;
import com.zhiyi.InstantChat.protobuf.ChatPkg.HeartBeatS2C;
import com.zhiyi.InstantChat.protobuf.ChatPkg.PkgS2C;
import com.zhiyi.InstantChat.protobuf.ChatPkg.RetCode;
import com.zhiyi.InstantChat.protobuf.ChatPkg.PkgS2C.PkgType;

public class HeartBeatHandler extends BaseHandler {
	private static final Logger logger = Logger.getLogger(HeartBeatHandler.class);
	
	@Override
	public void run() {
		PkgS2C.Builder pkgS2CBuilder = PkgS2C.newBuilder();
		pkgS2CBuilder.setType(PkgType.HEART_BEAT_ACK);
		HeartBeatS2C.Builder heartBeatS2CBuilder = HeartBeatS2C.newBuilder();
		HeartBeatC2S heartBeatC2S = pkgC2S.getHeartBeat();
		if ((heartBeatC2S == null) || (!heartBeatC2S.hasDeviceId() && !heartBeatC2S.hasUid())) {
			logger.warn("Illegal heartbeat packet: " + pkgC2S.toString());
			heartBeatS2CBuilder.setCode(RetCode.ILLEGAL_REQUEST);
			pkgS2CBuilder.setHeartBeatAck(heartBeatS2CBuilder.build());
			channel.writeAndFlush(pkgS2CBuilder.build());
			return;
		}
		
		String deviceId = heartBeatC2S.getDeviceId();
		Long userId = heartBeatC2S.getUid();
		OnlineClientMgr.getInstance().touch(ClientIdGenerator.genClientId(deviceId, userId));
		heartBeatS2CBuilder.setCode(RetCode.SUCCESS);
		heartBeatS2CBuilder.setSendTime(DateUtil.getCurrentMillisTimeUTC());
		pkgS2CBuilder.setHeartBeatAck(heartBeatS2CBuilder.build());
		logger.info("send to [deviceid:"
				+ deviceId + "; uid:" + userId + "]:\n" + pkgS2CBuilder.build().toString());
		channel.writeAndFlush(pkgS2CBuilder.build());
	}
	
}
