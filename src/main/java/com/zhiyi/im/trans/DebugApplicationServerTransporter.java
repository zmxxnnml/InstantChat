package com.zhiyi.im.trans;

import com.zhiyi.im.trans.entity.PushMsg;
import com.zhiyi.im.trans.exception.DeviceNotExistingException;
import com.zhiyi.im.trans.exception.InternalException;
import com.zhiyi.im.trans.exception.InvalidSecTokenException;
import com.zhiyi.im.trans.exception.UserNotExistingException;

public class DebugApplicationServerTransporter implements
		ApplicationServerTransporter {

	private static class AppServInteractorHolder {
		public static final DebugApplicationServerTransporter instance =
				new DebugApplicationServerTransporter();
	}
	
	public static DebugApplicationServerTransporter getInstance() {
		return AppServInteractorHolder.instance;
	}
	
	private DebugApplicationServerTransporter() {}
	
	@Override
	public void authenticateAppClient(Long uid, String deviceId, String secToken)
			throws InternalException, DeviceNotExistingException,
			UserNotExistingException, InvalidSecTokenException {
		// do nothing.
	}

	@Override
	public void sendNotificationToClient(Long uid, String deviceId, PushMsg msg)
			throws InternalException, DeviceNotExistingException,
			UserNotExistingException {
		// do nothing.
	}

}
