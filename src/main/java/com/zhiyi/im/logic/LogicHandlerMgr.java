package com.zhiyi.im.logic;

import com.zhiyi.im.protobuf.ChatPkg.PkgC2S;
import com.zhiyi.im.protobuf.ChatPkg.PkgC2S.PkgType;

public class LogicHandlerMgr {
	
	public static BaseHandler getHandler(PkgC2S.PkgType pkgType) {
		if (pkgType.equals(PkgType.REG)) {
			return new AuthHandler();
		}
		if (pkgType.equals(PkgType.HEART_BEAT)) {
			return new HeartBeatHandler();
		}
		if (pkgType.equals(PkgType.PULL_REQ)) {
			return new PullMsgHandler();
		}
		if (pkgType.equals(PkgType.MESSAGE)) {
			return new SendMsgHandler();
		}
		
		return null;
	}
}
