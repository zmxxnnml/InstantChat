package com.zhiyi.im.trans.exception;

public class DeviceNotExistingException extends Exception {

	private static final long serialVersionUID = -703373565184687645L;

	public DeviceNotExistingException() {
		super();
	}
	
	public DeviceNotExistingException(String msg) {
		super(msg);
	}
	
	public DeviceNotExistingException(Throwable cause) {
		super(cause);
	}
	
	public DeviceNotExistingException(String msg, Throwable cause) {
		super(msg, cause);
	}
}