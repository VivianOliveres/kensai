package com.kensai.animator.core;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kensai.animator.sdk.Animator;
import com.kensai.protocol.Trading.Messages;

public class AnimatorChannelHandler extends SimpleChannelHandler {

	private static final Logger log = LoggerFactory.getLogger(AnimatorChannelHandler.class);

	private final Animator animator;

	public AnimatorChannelHandler(Animator animator) {
		this.animator = animator;
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelConnected(ctx, e);
		animator.setMessageSender(new MessageSender(e.getChannel()));
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		super.messageReceived(ctx, e);
		log.info("messageReceived: [{}]", e.getMessage());
		Messages message = (Messages) e.getMessage();
		if (message.hasSubscribeCommand()) {
			animator.onSubscribe(message.getSubscribeCommand());
		}

		if (message.hasUnsubscribeCommand()) {
			animator.onUnsubscribe(message.getUnsubscribeCommand());
		}

		if (message.hasSummariesSnapshot()) {
			animator.onSnapshot(message.getSummariesSnapshot());
		}

		if (message.hasExecutionsSnapshot()) {
			animator.onSnapshot(message.getExecutionsSnapshot());
		}

		if (message.hasOrdersSnapshot()) {
			animator.onSnapshot(message.getOrdersSnapshot());
		}

		if (message.hasInstrumentsSnapshot()) {
			animator.onSnapshot(message.getInstrumentsSnapshot());
		}

		if (message.hasOrder()) {
			animator.onOrder(message.getOrder());
		}

		if (message.hasExecution()) {
			animator.onExecution(message.getExecution());
		}

		if (message.hasSummary()) {
			animator.onSummary(message.getSummary());
		}
	}
}