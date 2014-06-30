package com.kensai.gui.services.connectors;

import org.jboss.netty.channel.Channel;

import com.kensai.protocol.Trading.Messages;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.SubscribeCommand;
import com.kensai.protocol.Trading.UnsubscribeCommand;

public class MarketConnectorMessageSender {

	public void send(Channel connectedChannel, SubscribeCommand cmd) {
		Messages msg = Messages.newBuilder().setSubscribeCommand(cmd).build();
		connectedChannel.write(msg);
	}

	public void send(Channel connectedChannel, UnsubscribeCommand cmd) {
		Messages msg = Messages.newBuilder().setUnsubscribeCommand(cmd).build();
		connectedChannel.write(msg);
	}

	public void send(Channel connectedChannel, Order order) {
		Messages msg = Messages.newBuilder().setOrder(order).build();
		connectedChannel.write(msg);
	}

}
