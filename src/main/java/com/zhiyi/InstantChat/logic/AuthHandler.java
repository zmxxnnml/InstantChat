package com.zhiyi.InstantChat.logic;

import org.apache.log4j.Logger;

import com.zhiyi.InstantChat.base.DateUtil;
import com.zhiyi.InstantChat.client.OnlineClient;
import com.zhiyi.InstantChat.client.OnlineClientMgr;
import com.zhiyi.InstantChat.client.PendingClient;
import com.zhiyi.InstantChat.client.PendingClientMgr;
import com.zhiyi.InstantChat.inter.DefaultAppServInteractor;
import com.zhiyi.InstantChat.inter.exception.DeviceNotExistingException;
import com.zhiyi.InstantChat.inter.exception.InternalException;
import com.zhiyi.InstantChat.inter.exception.InvalidSecTokenException;
import com.zhiyi.InstantChat.inter.exception.UserNotExistingException;
import com.zhiyi.InstantChat.protobuf.ChatPkg.PkgS2C;
import com.zhiyi.InstantChat.protobuf.ChatPkg.PkgS2C.PkgType;
import com.zhiyi.InstantChat.protobuf.ChatPkg.RegC2S;
import com.zhiyi.InstantChat.protobuf.ChatPkg.RegS2C;
import com.zhiyi.InstantChat.protobuf.ChatPkg.RetCode;

/*
 * Use {uid/device_id/sec_token} to authorize app client.
 */
public class AuthHandler extends BaseHandler {
	private static final Logger logger = Logger.getLogger(AuthHandler.class);
	
	@Override
	public void run() {
		RegS2C.Builder regAckBuilder = RegS2C.newBuilder();
		
		RegC2S regc2s = pkgC2S.getReg();
		if ((regc2s == null) ||
				(!regc2s.hasDeviceId() && !regc2s.hasUid()) ||
				!regc2s.hasSecToken()) {
			regAckBuilder.setCode(RetCode.ILLEGAL_REQUEST);
			handleResp(regAckBuilder.build(), null, false);
			return;
		}
		
		Long uid = null;
		if (regc2s.hasUid()) {
			uid = regc2s.getUid();
		}
		String deviceId = null;
		if (regc2s.hasDeviceId()) {
			deviceId = regc2s.getDeviceId();
		}
		String secToken = null;
		if (regc2s.hasSecToken()) {
			secToken = regc2s.getSecToken();
		}
		
		boolean isAuthorized = true;
		try {
			DefaultAppServInteractor.getInstance().authenticateAppClient(uid, deviceId, secToken);
			regAckBuilder.setCode(RetCode.SUCCESS);
		} catch (InternalException e) {
			regAckBuilder.setCode(RetCode.INTERNAL_ERROR);
			isAuthorized = false;
		} catch (DeviceNotExistingException e) {
			regAckBuilder.setCode(RetCode.DEVICE_NOT_EXISTING);
			isAuthorized = false;
		} catch (UserNotExistingException e) {
			regAckBuilder.setCode(RetCode.USER_NOT_EXISING);
			isAuthorized = false;
		} catch (InvalidSecTokenException e) {
			regAckBuilder.setCode(RetCode.INVALID_SEC_TOKEN);
			isAuthorized = false;
		}
		
		// TODO: Just for debuging.
		regAckBuilder.setCode(RetCode.SUCCESS);
		isAuthorized = true;
		
		handleResp(regAckBuilder.build(), deviceId, isAuthorized);
	}
	
	private void handleResp(RegS2C regS2C, String deviceId, Boolean isAuthorized) {
		// Reg client on client mgr.
		if (isAuthorized) {
			// remove the channel from unauthorizedClientMgr.
			PendingClientMgr.getInstance().removeClient(channel.hashCode());
			
			OnlineClient client = new OnlineClient();
			client.setChannel(channel);
			client.setDeviceId(deviceId);
			client.setLastHeartBeatTime(DateUtil.getCurrentSecTimeUTC());
			OnlineClientMgr.getInstance().addClient(deviceId, client);
		} else {
			PendingClient unauthorizedAppClient =
					PendingClientMgr.getInstance().getClient(channel.hashCode());
			if (unauthorizedAppClient != null) {
				unauthorizedAppClient.setFailAuthorized(true);
			}
		}
		
		// Send resp to client
		PkgS2C.Builder pkgS2CBuilder = PkgS2C.newBuilder();
		pkgS2CBuilder.setType(PkgType.REG_ACK);
		pkgS2CBuilder.setRegAck(regS2C);
		
		logger.info("send to [" + deviceId + "]:\n" + pkgS2CBuilder.build().toString());
		
		channel.writeAndFlush(pkgS2CBuilder.build());
	}
}
