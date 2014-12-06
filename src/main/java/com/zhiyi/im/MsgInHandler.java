package com.zhiyi.im;

import org.apache.log4j.Logger;

import com.zhiyi.im.client.OnlineClientMgr;
import com.zhiyi.im.client.PendingClient;
import com.zhiyi.im.client.PendingClientMgr;
import com.zhiyi.im.common.DateUtil;
import com.zhiyi.im.logic.LogicDispatcher;
import com.zhiyi.im.protobuf.ChatPkg.PkgC2S;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MsgInHandler extends ChannelInboundHandlerAdapter {
	private static final Logger logger = Logger.getLogger(MsgInHandler.class);
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		logger.info("one client connected.");
		PendingClient client = new PendingClient();
		client.setChannel(ctx.channel());
		client.setConnectedTime(DateUtil.getCurrentMillisTimeUTC());
		PendingClientMgr.getInstance().addClient(client);
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx)
            throws Exception {
		logger.info("one client left.");
		OnlineClientMgr.getInstance().removeClient(ctx.channel());
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		PkgC2S pkg = (PkgC2S)msg;
		logger.info("received:\n" + pkg.toString());
		LogicDispatcher.submit(ctx.channel(), pkg);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		logger.warn("channel exception:" + cause.getMessage());
		OnlineClientMgr.getInstance().removeClient(ctx.channel());
		ctx.close();
	}
}
