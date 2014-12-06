package com.zhiyi.im.common.thread;

public interface Dispatcher {

	public void dispatch(Runnable r);

	public void stop();

}
