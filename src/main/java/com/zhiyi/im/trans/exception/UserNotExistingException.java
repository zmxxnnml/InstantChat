package com.zhiyi.im.trans.exception;

public class UserNotExistingException extends Exception {

	private static final long serialVersionUID = -703373565184687645L;

	public UserNotExistingException() {
		super();
	}
	
	public UserNotExistingException(String msg) {
		super(msg);
	}
	
	public UserNotExistingException(Throwable cause) {
		super(cause);
	}
	
	public UserNotExistingException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
