package com.kensai.gui.services.connectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.kensai.protocol.Trading.Messages;

public class GuiChannelHandler extends SimpleChannelHandler {

	private static final Logger log = LogManager.getLogger(GuiChannelHandler.class);
	private final MarketConnector conector;

	public GuiChannelHandler(MarketConnector conector) {
		this.conector = conector;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		log.error("exceptionCaught from ChannelHandler -> exit", e.getCause());
		System.exit(0);
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelConnected(ctx, e);
		log.info("Channel connected for " + conector.getMarketName());
		conector.channelConnected();
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelDisconnected(ctx, e);
		log.info("Channel disconnected for " + conector.getMarketName());
		conector.channelDisconnected();
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		super.messageReceived(ctx, e);
		log.info("messageReceived: [{}]", e.getMessage());
		Messages message = (Messages) e.getMessage();
		if (message.hasSubscribeCommand()) {
			conector.onSubscribe(message.getSubscribeCommand());
		}

		if (message.hasUnsubscribeCommand()) {
			conector.onUnsubscribe(message.getUnsubscribeCommand());
		}

		if (message.hasSummariesSnapshot()) {
			conector.onSnapshot(message.getSummariesSnapshot());
		}

		if (message.hasExecutionsSnapshot()) {
			conector.onSnapshot(message.getExecutionsSnapshot());
		}

		if (message.hasOrdersSnapshot()) {
			conector.onSnapshot(message.getOrdersSnapshot());
		}

		if (message.hasInstrumentsSnapshot()) {
			conector.onSnapshot(message.getInstrumentsSnapshot());
		}

		if (message.hasOrder()) {
			conector.onOrder(message.getOrder());
		}

		if (message.hasExecution()) {
			conector.onExecution(message.getExecution());
		}

		if (message.hasSummary()) {
			conector.onSummary(message.getSummary());
		}
	}
}
