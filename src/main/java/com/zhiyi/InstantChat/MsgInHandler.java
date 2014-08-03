package com.zhiyi.InstantChat;

import com.zhiyi.InstantChat.base.DateUtil;
import com.zhiyi.InstantChat.client.OnlineClientMgr;
import com.zhiyi.InstantChat.client.UnauthorizedAppClient;
import com.zhiyi.InstantChat.client.UnauthorizedClientMgr;
import com.zhiyi.InstantChat.logic.LogicDispatcher;
import com.zhiyi.InstantChat.protobuf.ChatPkg.PkgC2S;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MsgInHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		UnauthorizedAppClient client = new UnauthorizedAppClient();
		client.setChannel(ctx.channel());
		client.setConnectedTime(DateUtil.getCurrentSecTimeUTC());
		UnauthorizedClientMgr.getInstance().addClient(client);
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx)
            throws Exception {
		OnlineClientMgr.getInstance().removeClient(ctx.channel());
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		PkgC2S pkg = (PkgC2S)msg;
		LogicDispatcher.submit(ctx.channel(), pkg);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		OnlineClientMgr.getInstance().removeClient(ctx.channel());
		ctx.close();
	}
}
