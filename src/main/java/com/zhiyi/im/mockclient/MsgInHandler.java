package com.zhiyi.im.mockclient;

import org.apache.log4j.Logger;

import com.zhiyi.im.protobuf.ChatPkg.PkgS2C;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MsgInHandler extends ChannelInboundHandlerAdapter {
	
	private static final Logger logger = Logger.getLogger(MsgInHandler.class);
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		logger.info("channelActive.");
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx)
            throws Exception {
		logger.info("channelInactive.");
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		// Get data from server: REG/HEARTBEAT/PULLMESSAGE
		PkgS2C pkg = (PkgS2C)msg;
		logger.info("received:\n" + pkg.toString());
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		logger.error("Channel exception:", cause);
		ctx.close();
	}
}
