package com.zhiyi.InstantChat.logic;

import com.zhiyi.InstantChat.inter.AppServInteractor;
import com.zhiyi.InstantChat.inter.exception.DeviceNotExistingException;
import com.zhiyi.InstantChat.inter.exception.InternalException;
import com.zhiyi.InstantChat.inter.exception.InvalidSecTokenException;
import com.zhiyi.InstantChat.inter.exception.UserNotExistingException;
import com.zhiyi.InstantChat.protobuf.ChatPkg.RegC2S;
import com.zhiyi.InstantChat.protobuf.ChatPkg.RegS2C;
import com.zhiyi.InstantChat.protobuf.ChatPkg.RegS2C.RetCode;

/*
 * Use {uid/device_id/sec_token} to validate app client.
 */
public class AuthHandler extends BaseHandler {
	@Override
	public void run() {
		RegC2S regc2s = pkgC2S.getReg();
		if (regc2s == null) {
			// do nothing
			return;
		}
		
		long uid = regc2s.getUid();
		String deviceId = regc2s.getDeviceId();
		String secToken = regc2s.getSecToken();
		
		RegS2C.Builder regAckBuilder = RegS2C.newBuilder();
		regAckBuilder.setCode(RetCode.SUCCESS);
		try {
			AppServInteractor.getInstance().authenticateAppClient(uid, deviceId, secToken);
		} catch (InternalException e) {
			regAckBuilder.setCode(RetCode.INTERNAL_ERROR);
		} catch (DeviceNotExistingException e) {
			regAckBuilder.setCode(RetCode.DEVICE_NOT_EXISTING);
		} catch (UserNotExistingException e) {
			regAckBuilder.setCode(RetCode.USER_NOT_EXISING);
		} catch (InvalidSecTokenException e) {
			regAckBuilder.setCode(RetCode.INVALID_SEC_TOKEN);
		}
		
		// Send resp to client
		channel.write(regAckBuilder.build());
	}
}
