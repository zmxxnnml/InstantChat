package com.zhiyi.InstantChat.logic;

import org.apache.log4j.Logger;

import com.zhiyi.InstantChat.base.DateUtil;
import com.zhiyi.InstantChat.client.OnlineClientMgr;
import com.zhiyi.InstantChat.exception.ClientNotExistingException;
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
		if ((heartBeatC2S == null) || (!heartBeatC2S.hasDeviceId())) {
			// TODO: log ERROR
			heartBeatS2CBuilder.setCode(RetCode.ILLEGAL_REQUEST);
			pkgS2CBuilder.setHeartBeatAck(heartBeatS2CBuilder.build());
			logger.info("send:\n" + pkgS2CBuilder.build().toString());
			channel.writeAndFlush(pkgS2CBuilder.build());
			return;
		}
		
		String deviceId = null;
		if (heartBeatC2S.hasDeviceId()) {
			deviceId = heartBeatC2S.getDeviceId();
		}
		
		long lastHeartBeatTime = DateUtil.getCurrentSecTimeUTC();
		try {
			OnlineClientMgr.getInstance().refreshClientHeartBeat(deviceId, lastHeartBeatTime);
			heartBeatS2CBuilder.setCode(RetCode.SUCCESS);
			heartBeatS2CBuilder.setSendTime(lastHeartBeatTime);
		} catch (ClientNotExistingException e) {
			heartBeatS2CBuilder.setCode(RetCode.DEVICE_NOT_EXISTING);
		}
		
		pkgS2CBuilder.setHeartBeatAck(heartBeatS2CBuilder.build());
		logger.info("send to [" + deviceId + "]:\n" + pkgS2CBuilder.build().toString());
		channel.writeAndFlush(pkgS2CBuilder.build());
	}
	
}
