package com.kensai.animator.core;

import org.jboss.netty.channel.Channel;

import com.kensai.protocol.Trading.Messages;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.SubscribeCommand;
import com.kensai.protocol.Trading.UnsubscribeCommand;

public class MessageSender {

	private final Channel channel;

	public MessageSender(Channel channel) {
		this.channel = channel;
	}

	public void send(SubscribeCommand cmd) {
		Messages msg = Messages.newBuilder().setSubscribeCommand(cmd).build();
		channel.write(msg);
	}

	public void send(UnsubscribeCommand cmd) {
		Messages msg = Messages.newBuilder().setUnsubscribeCommand(cmd).build();
		channel.write(msg);
	}

	public void send(Order order) {
		Messages msg = Messages.newBuilder().setOrder(order).build();
		channel.write(msg);
	}

}
