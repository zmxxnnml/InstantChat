package com.zhiyi.im.trans;

import com.zhiyi.im.trans.entity.PushMsg;
import com.zhiyi.im.trans.exception.DeviceNotExistingException;
import com.zhiyi.im.trans.exception.InternalException;
import com.zhiyi.im.trans.exception.InvalidSecTokenException;
import com.zhiyi.im.trans.exception.UserNotExistingException;

public interface ApplicationServerTransporter {
	
	public void authenticateAppClient(Long uid, String deviceId, String secToken) throws InternalException,
		DeviceNotExistingException, UserNotExistingException, InvalidSecTokenException;
	
	public void sendNotificationToClient(Long uid, String deviceId, PushMsg msg)
			throws InternalException, DeviceNotExistingException, UserNotExistingException;
	
}
