package com.zhiyi.InstantChat.trans.entity;

public enum TransErrorCode {
	USER_NOT_EXISTING(1),
	DEVICE_NOT_EXISTING(2),
	INVALID_SEC_TOKEN(3);
	
	private TransErrorCode(int v) {
		this.value = v;
	}
	
	public Integer toInteger() {
		return value;
	}
	
	public String toString() {
		// TODO
		return "";
	}
	
	int value;
};
