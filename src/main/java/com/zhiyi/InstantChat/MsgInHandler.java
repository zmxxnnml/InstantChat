package com.zhiyi.InstantChat;

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
		// TODO: do business logic
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
