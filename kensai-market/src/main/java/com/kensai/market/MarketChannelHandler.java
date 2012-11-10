package com.kensai.market;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kensai.protocol.Trading.CommandStatus;
import com.kensai.protocol.Trading.ExecutionsSnapshot;
import com.kensai.protocol.Trading.InstrumentsSnapshot;
import com.kensai.protocol.Trading.Messages;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.OrdersSnapshot;
import com.kensai.protocol.Trading.SubscribeCommand;
import com.kensai.protocol.Trading.SummariesSnapshot;
import com.kensai.protocol.Trading.UnsubscribeCommand;

public class MarketChannelHandler extends SimpleChannelHandler {

	private static final Logger log = LoggerFactory.getLogger(MarketChannelHandler.class);

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		log.debug("New message received [{}]", e.getMessage());
		Messages messages = (Messages) e.getMessage();
		if (messages.hasSubscribeCommand()) {
			sendMessageAck(messages.getSubscribeCommand(), e.getChannel());
			sendInstrumentsSnapshot(e.getChannel());
			sendSummariesSnapshot(e.getChannel());
			sendOrdersSnapshot(e.getChannel());
			sendExecutionsSnapshot(e.getChannel());

		} else if (messages.hasUnsubscribeCommand()) {
			sendMessageAck(messages.getUnsubscribeCommand(), e.getChannel());
		}

		if (messages.hasOrder()) {
			sendMessageAck(messages.getOrder(), e.getChannel());
		}

		log.warn("Invalid message: {}", messages);
	}

	private void sendMessageAck(Order order, Channel channel) {
		Order response = Order.newBuilder(order).setInsertTime(System.currentTimeMillis()).setId(123L).setLastUpdateTime(System.currentTimeMillis())
			.build();
		Messages msg = Messages.newBuilder().setOrder(response).build();
		channel.write(msg);
	}

	private void sendMessageAck(SubscribeCommand subscribeCommand, Channel channel) {
		SubscribeCommand cmd = SubscribeCommand.newBuilder(subscribeCommand).setStatus(CommandStatus.ACK).build();
		Messages msg = Messages.newBuilder().setSubscribeCommand(cmd).build();
		channel.write(msg);
	}

	private void sendMessageAck(UnsubscribeCommand unsubscribeCommand, Channel channel) {
		UnsubscribeCommand cmd = UnsubscribeCommand.newBuilder(unsubscribeCommand).setStatus(CommandStatus.ACK).build();
		Messages msg = Messages.newBuilder().setUnsubscribeCommand(cmd).build();
		channel.write(msg);
	}

	private void sendInstrumentsSnapshot(Channel channel) {
		InstrumentsSnapshot snapshot = InstrumentsSnapshot.newBuilder().build();
		Messages msg = Messages.newBuilder().setInstrumentsSnapshot(snapshot).build();
		channel.write(msg);
	}

	private void sendSummariesSnapshot(Channel channel) {
		SummariesSnapshot snapshot = SummariesSnapshot.newBuilder().build();
		Messages msg = Messages.newBuilder().setSummariesSnapshot(snapshot).build();
		channel.write(msg);
	}

	private void sendOrdersSnapshot(Channel channel) {
		OrdersSnapshot snapshot = OrdersSnapshot.newBuilder().build();
		Messages msg = Messages.newBuilder().setOrdersSnapshot(snapshot).build();
		channel.write(msg);
	}

	private void sendExecutionsSnapshot(Channel channel) {
		ExecutionsSnapshot snapshot = ExecutionsSnapshot.newBuilder().build();
		Messages msg = Messages.newBuilder().setExecutionsSnapshot(snapshot).build();
		channel.write(msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		log.error("Exception caught", e.getCause());

		// TODO
		Channel ch = e.getChannel();
		ch.close();
	}
}
