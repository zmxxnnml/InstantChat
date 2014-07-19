package com.zhiyi.InstantChat.base;

public class DateUtil {
	public static final long getCurrentSecTimeUTC() {
		return System.currentTimeMillis() / 1000;
	}
	
	public static final long getCurrentMillisTimeUTC() {
		return System.currentTimeMillis();
	}
	
}
