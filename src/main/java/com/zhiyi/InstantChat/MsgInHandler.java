package com.zhiyi.InstantChat;

import com.zhiyi.InstantChat.logic.LogicDispatcher;
import com.zhiyi.InstantChat.protobuf.ChatPkg.PkgC2S;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MsgInHandler extends ChannelInboundHandlerAdapter {
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx)
            throws Exception {
		ClientMgr.getInstance().removeClient(ctx.channel());
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		PkgC2S pkg = (PkgC2S)msg;
		LogicDispatcher.submit(ctx.channel(), pkg);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ClientMgr.getInstance().removeClient(ctx.channel());
		ctx.close();
	}
}
