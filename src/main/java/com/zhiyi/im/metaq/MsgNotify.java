package com.zhiyi.im.metaq;

import java.io.Serializable;

public class MsgNotify implements Serializable {

	private static final long serialVersionUID = -2172131634552408587L;

	private long fromUid;
	
	private String fromDeviceId;
	
	private long toUid;
	
	private String toDeviceId;

	public long getFromUid() {
		return fromUid;
	}

	public void setFromUid(long fromUid) {
		this.fromUid = fromUid;
	}

	public String getFromDeviceId() {
		return fromDeviceId;
	}

	public void setFromDeviceId(String fromDeviceId) {
		this.fromDeviceId = fromDeviceId;
	}

	public long getToUid() {
		return toUid;
	}

	public void setToUid(long toUid) {
		this.toUid = toUid;
	}

	public String getToDeviceId() {
		return toDeviceId;
	}

	public void setToDeviceId(String toDeviceId) {
		this.toDeviceId = toDeviceId;
	}
	
}
