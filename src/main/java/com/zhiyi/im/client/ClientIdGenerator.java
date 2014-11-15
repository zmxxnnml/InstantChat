package com.zhiyi.im.client;

/**
 * Generate client ID by deviceId or userId.
 * We use an uniform identifier (client ID) to identify a client.
 */
public class ClientIdGenerator {
	
	// TODO:
	// Currently we just use deviceId as clientId.
	public static final String genClientId(String deviceId, Long userId) {
		return deviceId;
	}
	
}
