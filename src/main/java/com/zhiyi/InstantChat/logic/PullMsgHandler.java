package com.zhiyi.InstantChat.logic;

import com.zhiyi.InstantChat.protobuf.ChatPkg.PkgS2C;
import com.zhiyi.InstantChat.protobuf.ChatPkg.PullMessageS2C;
import com.zhiyi.InstantChat.protobuf.ChatPkg.PullReqC2S;
import com.zhiyi.InstantChat.protobuf.ChatPkg.PkgS2C.PkgType;
import com.zhiyi.InstantChat.protobuf.ChatPkg.RetCode;

public class PullMsgHandler extends BaseHandler {
	@Override
	public void run() {
		PkgS2C.Builder pkgS2CBuilder = PkgS2C.newBuilder();
		pkgS2CBuilder.setType(PkgType.MESSAGE);
		
		PullMessageS2C.Builder pullMessageS2C = PullMessageS2C.newBuilder();
		if (!pkgC2S.hasPullReq()) {
			// TODO: log ERROR
			pullMessageS2C.setCode(RetCode.ILLEGAL_REQUEST);
			pkgS2CBuilder.setPullMsgAck(pullMessageS2C.build());
			channel.write(pkgS2CBuilder.build());
			return;
		}
		
		PullReqC2S pullReqC2S = pkgC2S.getPullReq();
		if (!pullReqC2S.hasDeviceId()) {
			pullMessageS2C.setCode(RetCode.ILLEGAL_REQUEST);
			pkgS2CBuilder.setPullMsgAck(pullMessageS2C.build());
			channel.write(pkgS2CBuilder.build());
			return;
		}
		
		// TODO: interact with MongoDB
		// 1. Parse out of req_start_seq/req_end_seq/ack_seq
		// 2. Get messages from MongoDB
		// 3. Update messages as has-read
		// 4. Update seq(deviceId/uid)
	}
}
