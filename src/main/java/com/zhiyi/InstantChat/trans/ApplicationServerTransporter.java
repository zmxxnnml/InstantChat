package com.zhiyi.InstantChat.trans;

import com.zhiyi.InstantChat.trans.entity.PushMsg;
import com.zhiyi.InstantChat.trans.exception.DeviceNotExistingException;
import com.zhiyi.InstantChat.trans.exception.InternalException;
import com.zhiyi.InstantChat.trans.exception.InvalidSecTokenException;
import com.zhiyi.InstantChat.trans.exception.UserNotExistingException;

public interface ApplicationServerTransporter {
	
	public void authenticateAppClient(Long uid, String deviceId, String secToken) throws InternalException,
		DeviceNotExistingException, UserNotExistingException, InvalidSecTokenException;
	
	public void sendNotificationToClient(Long uid, String deviceId, PushMsg msg)
			throws InternalException, DeviceNotExistingException, UserNotExistingException;
	
}
