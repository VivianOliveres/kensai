package com.kensai.market.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.kensai.market.core.KensaiMarket;
import com.kensai.protocol.Trading.Messages;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.SubscribeCommand;
import com.kensai.protocol.Trading.UnsubscribeCommand;

public class MarketChannelHandler extends SimpleChannelHandler {

	private static final Logger log = LogManager.getLogger(MarketChannelHandler.class);

	private final KensaiMarket core;

	public MarketChannelHandler(KensaiMarket core) {
		this.core = core;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		Channel channel = e.getChannel();
		Messages msg = (Messages) e.getMessage();
		log.debug("Channel has received new message [{}]", msg);

		// Precondition: check message validity
		if (msg.hasSubscribeCommand()) {
			SubscribeCommand cmd = msg.getSubscribeCommand();
			log.info("Channel has received SubscribeCommand from [{}]", cmd.getUser().getName());
			core.receivedSubscribe(cmd, channel);

		} else if (msg.hasUnsubscribeCommand()) {
			UnsubscribeCommand cmd = msg.getUnsubscribeCommand();
			log.info("Channel has received UnsubscribeCommand from [{}]", cmd.getUser().getName());
			core.receivedUnsubscribed(cmd, channel);
		}

		if (msg.hasOrder()) {
			Order order = msg.getOrder();
			log.info("Channel has received Order from [{}]", order.getUser().getName());
			core.receivedOrder(order, channel);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		log.error("Exception caught", e.getCause());
	}
}
