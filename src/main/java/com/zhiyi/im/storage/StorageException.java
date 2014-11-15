package com.zhiyi.im.storage;

public class StorageException extends Exception {

	private static final long serialVersionUID = -703373565184687645L;

	public StorageException() {
		super();
	}
	
	public StorageException(String msg) {
		super(msg);
	}
	
	public StorageException(Throwable cause) {
		super(cause);
	}
	
	public StorageException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
