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
		log.debug("messageReceived: [{}]", e.getMessage());
		Messages message = (Messages) e.getMessage();
		if (message.hasSubscribeCommand()) {
			log.info("messageReceived: SubscribeCommand");
			conector.getMessageHandler().onSubscribe(message.getSubscribeCommand());
		}

		if (message.hasUnsubscribeCommand()) {
			log.info("messageReceived: UnsubscribeCommand");
			conector.getMessageHandler().onUnsubscribe(message.getUnsubscribeCommand());
		}

		if (message.hasSummariesSnapshot()) {
			log.info("messageReceived: SummariesSnapshot on [{}] summaries", message.getSummariesSnapshot().getSummariesCount());
			conector.getMessageHandler().onSnapshot(message.getSummariesSnapshot());
		}

		if (message.hasExecutionsSnapshot()) {
			log.info("messageReceived: ExecutionsSnapshot on [{}] executions", message.getExecutionsSnapshot().getExecutionsCount());
			conector.getMessageHandler().onSnapshot(message.getExecutionsSnapshot());
		}

		if (message.hasOrdersSnapshot()) {
			log.info("messageReceived: OrdersSnapshot on [{}] order", message.getOrdersSnapshot().getOrdersCount());
			conector.getMessageHandler().onSnapshot(message.getOrdersSnapshot());
		}

		if (message.hasInstrumentsSnapshot()) {
			log.info("messageReceived: InstrumentsSnapshot on [{}] instruments", message.getInstrumentsSnapshot().getInstrumentsCount());
			conector.getMessageHandler().onSnapshot(message.getInstrumentsSnapshot());
		}

		if (message.hasOrder()) {
			log.info("messageReceived: Order on [{}]", message.getOrder().getInstrument().getName());
			conector.getMessageHandler().onOrder(message.getOrder());
		}

		if (message.hasExecution()) {
			log.info("messageReceived: Execution on [{}]", message.getExecution().getOrder().getInstrument().getName());
			conector.getMessageHandler().onExecution(message.getExecution());
		}

		if (message.hasSummary()) {
			log.debug("messageReceived: Summary on [{}]", message.getSummary().getInstrument().getName());
			conector.getMessageHandler().onSummary(message.getSummary());
		}
	}
}
