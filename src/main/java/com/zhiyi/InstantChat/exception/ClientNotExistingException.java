package com.zhiyi.InstantChat.exception;

public class ClientNotExistingException extends Exception {

	private static final long serialVersionUID = -703373565184687645L;

	public ClientNotExistingException() {
		super();
	}
	
	public ClientNotExistingException(String msg) {
		super(msg);
	}
	
	public ClientNotExistingException(Throwable cause) {
		super(cause);
	}
	
	public ClientNotExistingException(String msg, Throwable cause) {
		super(msg, cause);
	}
}