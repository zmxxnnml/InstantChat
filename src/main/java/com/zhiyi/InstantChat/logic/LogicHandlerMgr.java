package com.zhiyi.InstantChat.logic;

import com.zhiyi.InstantChat.protobuf.ChatPkg.PkgC2S;
import com.zhiyi.InstantChat.protobuf.ChatPkg.PkgC2S.PkgType;

public class LogicHandlerMgr {
	
	public static BaseHandler getHandler(PkgC2S.PkgType pkgType) {
		if (pkgType == PkgType.REG) {
			return new AuthHandler();
		}
		if (pkgType == PkgType.HEART_BEAT) {
			return new HeartBeatHandler();
		}
		if (pkgType == PkgType.PULL_REQ) {
			return new PullMsgHandler();
		}
		if (pkgType == PkgType.MESSAGE) {
			return new SendMsgHandler();
		}
		
		return null;
	}
}
