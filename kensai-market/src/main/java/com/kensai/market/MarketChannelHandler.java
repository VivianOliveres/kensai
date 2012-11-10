package com.kensai.market;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kensai.protocol.Trading.Messages;
import com.kensai.protocol.Trading.Order;

public class MarketChannelHandler extends SimpleChannelHandler {

	private static final Logger log = LoggerFactory.getLogger(MarketChannelHandler.class);

	private final KensaiMarket core;

	public MarketChannelHandler(KensaiMarket core) {
		this.core = core;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		Channel channel = e.getChannel();
		Messages msg = (Messages) e.getMessage();
		log.debug("Channel has received a new message [{}]", msg);

		// Precondition: check message validity
		if (msg.hasSubscribeCommand()) {
			core.doSubscribeCommand(msg.getSubscribeCommand(), channel);

		} else if (msg.hasUnsubscribeCommand()) {
			core.doUnsubscribeCommand(msg.getUnsubscribeCommand(), channel);
		}

		if (msg.hasOrder()) {
			Order order = msg.getOrder();
			core.doOrder(order, channel);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		log.error("Exception caught", e.getCause());
	}
}
