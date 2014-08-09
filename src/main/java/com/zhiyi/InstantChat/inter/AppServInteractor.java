package com.zhiyi.InstantChat.inter;

import com.zhiyi.InstantChat.inter.exception.DeviceNotExistingException;
import com.zhiyi.InstantChat.inter.exception.InternalException;
import com.zhiyi.InstantChat.inter.exception.InvalidSecTokenException;
import com.zhiyi.InstantChat.inter.exception.UserNotExistingException;

public interface AppServInteractor {
	
	public void authenticateAppClient(Long uid, String deviceId, String secToken)
			throws InternalException, DeviceNotExistingException,
			UserNotExistingException, InvalidSecTokenException;
	
}
