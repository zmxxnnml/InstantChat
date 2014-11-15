package com.zhiyi.im.trans.exception;

public class InternalException extends Exception {

	private static final long serialVersionUID = -703373565184687645L;

	public InternalException() {
		super();
	}
	
	public InternalException(String msg) {
		super(msg);
	}
	
	public InternalException(Throwable cause) {
		super(cause);
	}
	
	public InternalException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
