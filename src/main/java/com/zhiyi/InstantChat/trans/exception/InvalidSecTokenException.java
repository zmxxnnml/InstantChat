package com.zhiyi.InstantChat.trans.exception;

public class InvalidSecTokenException extends Exception {

	private static final long serialVersionUID = -703373565184687645L;

	public InvalidSecTokenException() {
		super();
	}
	
	public InvalidSecTokenException(String msg) {
		super(msg);
	}
	
	public InvalidSecTokenException(Throwable cause) {
		super(cause);
	}
	
	public InvalidSecTokenException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
