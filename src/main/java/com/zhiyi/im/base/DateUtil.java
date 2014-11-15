package com.zhiyi.im.base;

public class DateUtil {
	public static final long getCurrentSecTimeUTC() {
		return System.currentTimeMillis() / 1000;
	}
	
	public static final long getCurrentMillisTimeUTC() {
		return System.currentTimeMillis();
	}
	
}
