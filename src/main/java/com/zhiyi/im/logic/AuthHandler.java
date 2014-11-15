package com.zhiyi.im.logic;

import org.apache.log4j.Logger;

import com.zhiyi.im.client.ClientIdGenerator;
import com.zhiyi.im.client.OnlineClientMgr;
import com.zhiyi.im.client.PendingClient;
import com.zhiyi.im.client.PendingClientMgr;
import com.zhiyi.im.config.InstantChatConfig;
import com.zhiyi.im.protobuf.ChatPkg.PkgS2C;
import com.zhiyi.im.protobuf.ChatPkg.RegC2S;
import com.zhiyi.im.protobuf.ChatPkg.RegS2C;
import com.zhiyi.im.protobuf.ChatPkg.RetCode;
import com.zhiyi.im.protobuf.ChatPkg.PkgS2C.PkgType;
import com.zhiyi.im.trans.ApplicationServerTransporter;
import com.zhiyi.im.trans.ApplicationServerTransporterFactory;
import com.zhiyi.im.trans.exception.DeviceNotExistingException;
import com.zhiyi.im.trans.exception.InternalException;
import com.zhiyi.im.trans.exception.InvalidSecTokenException;
import com.zhiyi.im.trans.exception.UserNotExistingException;

/*
 * Use {uid/device_id/sec_token} to authorize app client.
 */
public class AuthHandler extends BaseHandler {
	private static final Logger logger = Logger.getLogger(AuthHandler.class);
	
	@Override
	public void run() {
		RegS2C.Builder regAckBuilder = RegS2C.newBuilder();
		RegC2S regc2s = pkgC2S.getReg();
		if ((regc2s == null) || !regc2s.hasSecToken() ||
				(!regc2s.hasDeviceId() && !regc2s.hasUid())) {
			logger.warn("Illegal auth packet: " + pkgC2S.toString());
			regAckBuilder.setCode(RetCode.ILLEGAL_REQUEST);
			handleResp(regAckBuilder.build(), null, false);
			return;
		}
		
		Long uid = regc2s.getUid();
		String deviceId =  regc2s.getDeviceId();
		String secToken = regc2s.getSecToken();
		boolean isAuthorized = true;
		try {
			ApplicationServerTransporter transporter  =
					ApplicationServerTransporterFactory.getTransporter(
					InstantChatConfig.getInstance().getApplicationServerType());
			transporter.authenticateAppClient(uid, deviceId, secToken);
			regAckBuilder.setCode(RetCode.SUCCESS);
		} catch (InternalException e) {
			regAckBuilder.setCode(RetCode.INTERNAL_ERROR);
			isAuthorized = false;
			logger.warn("Reg failed  by internal error: " + e);
		} catch (DeviceNotExistingException e) {
			regAckBuilder.setCode(RetCode.DEVICE_NOT_EXISTING);
			isAuthorized = false;
			logger.warn("Reg failed  by DeviceNotExisting: " + pkgC2S.toString());
		} catch (UserNotExistingException e) {
			regAckBuilder.setCode(RetCode.USER_NOT_EXISING);
			isAuthorized = false;
			logger.warn("Reg failed  by UserNotExisting: "  + pkgC2S.toString());
		} catch (InvalidSecTokenException e) {
			regAckBuilder.setCode(RetCode.INVALID_SEC_TOKEN);
			isAuthorized = false;
			logger.warn("Reg failed  by InvalidSecToken: "  + pkgC2S.toString());
		}

		handleResp(
				regAckBuilder.build(),
				ClientIdGenerator.genClientId(regc2s.getDeviceId(), regc2s.getUid()),
				isAuthorized);
	}
	
	private void handleResp(RegS2C regS2C, String clientId, Boolean isAuthorized) {
		if (isAuthorized) {
			// Remove the channel from unauthorized client pool.
			PendingClientMgr.getInstance().removeClient(channel.hashCode());
			OnlineClientMgr.getInstance().addClient(clientId, channel);
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
		
		logger.info("send to [" + clientId + "]:\n" + pkgS2CBuilder.build().toString());
		
		channel.writeAndFlush(pkgS2CBuilder.build());
	}
}
