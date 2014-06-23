package com.kensai.animator.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.kensai.animator.sdk.Animator;
import com.kensai.protocol.Trading.Messages;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.Summary;

public class AnimatorChannelHandler extends SimpleChannelHandler {

	private static final Logger log = LogManager.getLogger(AnimatorChannelHandler.class);

	private final Animator animator;

	public AnimatorChannelHandler(Animator animator) {
		this.animator = animator;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		log.error("exceptionCaught from ChannelHandler -> exit", e.getCause());
		System.exit(0);
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelConnected(ctx, e);
		log.info("Channel connected");
		animator.setMessageSender(new MessageSender(e.getChannel()));
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		super.messageReceived(ctx, e);
		log.debug("messageReceived: [{}]", e.getMessage());
		Messages message = (Messages) e.getMessage();
		if (message.hasSubscribeCommand()) {
			animator.onSubscribe(message.getSubscribeCommand());
		}

		if (message.hasUnsubscribeCommand()) {
			animator.onUnsubscribe(message.getUnsubscribeCommand());
		}

		if (message.hasSummariesSnapshot()) {
			log.info("summariesSnapshot received: [{}]", message.getSummariesSnapshot().getSummariesCount());
			animator.onSnapshot(message.getSummariesSnapshot());
		}

		if (message.hasExecutionsSnapshot()) {
			log.info("executionsSnapshot received: [{}]", message.getExecutionsSnapshot().getExecutionsCount());
			animator.onSnapshot(message.getExecutionsSnapshot());
		}

		if (message.hasOrdersSnapshot()) {
			log.info("ordersSnapshot received: [{}]", message.getOrdersSnapshot().getOrdersCount());
			animator.onSnapshot(message.getOrdersSnapshot());
		}

		if (message.hasInstrumentsSnapshot()) {
			log.info("instrumentsSnapshot received: [{}]", message.getInstrumentsSnapshot().getInstrumentsCount());
			animator.onSnapshot(message.getInstrumentsSnapshot());
		}

		if (message.hasOrder()) {
			Order order = message.getOrder();
			log.info("order received: [{}] instr[{}] side[{}] userData[{}] price[{}] qty[{}/{}]", order.getId(), order.getInstrument().getName(),
				order.getSide(), order.getUserData(), order.getPrice(), order.getExecutedQuantity(), order.getInitialQuantity());
			animator.onOrder(order);
		}

		if (message.hasExecution()) {
			Order order = message.getExecution().getOrder();
			log.info("execution received: [{}] on [{}]", order.getId(), order.getInstrument().getName());
			animator.onExecution(message.getExecution());
		}

		if (message.hasSummary()) {
			Summary summary = message.getSummary();
			log.debug("summary received: [{}]", summary.getInstrument().getName());
			animator.onSummary(summary);
		}
	}
}
