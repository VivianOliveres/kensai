package com.kensai.market;

import java.util.List;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kensai.trading.Trading.CommandStatus;
import com.kensai.trading.Trading.Execution;
import com.kensai.trading.Trading.Instrument;
import com.kensai.trading.Trading.Messages;
import com.kensai.trading.Trading.Order;
import com.kensai.trading.Trading.SubscribeCommand;
import com.kensai.trading.Trading.Summary;
import com.kensai.trading.Trading.UnsubscribeCommand;

public class MarketClientChannelHandler extends SimpleChannelHandler {

	private static final Logger log = LoggerFactory.getLogger(MarketClientChannelHandler.class);

	private Channel channel;

	private boolean hasReceiveSubscribeAnswer;
	private boolean isSubscribeOk;

	private boolean hasReceiveUnsubscribeAnswer;
	private boolean isUnsubscribeOk;

	private boolean hasReceivedOrder;
	private boolean isOrderOk;

	private List<Summary> summariesSnapshot;
	private List<Execution> executionsSnapshot;
	private List<Order> ordersSnapshot;
	private List<Instrument> instrumentsSnapshot;

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelConnected(ctx, e);
		channel = e.getChannel();
	}

	public void sendSubscribe() {
		log.info("sendSubscribe");
		SubscribeCommand command = SubscribeCommand.newBuilder().setUser("user-client").build();
		Messages msg = Messages.newBuilder().setSubscribeCommand(command).build();
		channel.write(msg);
	}

	public void sendUnsubscribe() {
		log.info("sendUnsubscribe");
		UnsubscribeCommand command = UnsubscribeCommand.newBuilder().setUser("user-client").build();
		Messages msg = Messages.newBuilder().setUnsubscribeCommand(command).build();
		channel.write(msg);
	}

	public void sendOrder(Order order) {
		log.info("sendOrder [{}]", order);
		Messages msg = Messages.newBuilder().setOrder(order).build();
		channel.write(msg);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		super.messageReceived(ctx, e);
		log.info("messageReceived: [{}]", e.getMessage());
		Messages message = (Messages) e.getMessage();
		if (message.hasSubscribeCommand()) {
			hasReceiveSubscribeAnswer = true;
			isSubscribeOk = message.getSubscribeCommand().getStatus().equals(CommandStatus.ACK);
		}

		if (message.hasUnsubscribeCommand()) {
			hasReceiveUnsubscribeAnswer = true;
			isUnsubscribeOk = message.getUnsubscribeCommand().getStatus().equals(CommandStatus.ACK);
		}

		if (message.hasSummariesSnapshot()) {
			summariesSnapshot = message.getSummariesSnapshot().getSummariesList();
		}

		if (message.hasExecutionsSnapshot()) {
			executionsSnapshot = message.getExecutionsSnapshot().getExecutionsList();
		}

		if (message.hasOrdersSnapshot()) {
			ordersSnapshot = message.getOrdersSnapshot().getOrdersList();
		}

		if (message.hasInstrumentsSnapshot()) {
			instrumentsSnapshot = message.getInstrumentsSnapshot().getInstrumentsList();
		}

		if (message.hasOrder()) {
			hasReceivedOrder = true;
			isOrderOk = message.getOrder().getStatus().equals(CommandStatus.ACK);
		}
	}

	public boolean hasReceiveSubscribeAnswer() {
		return hasReceiveSubscribeAnswer;
	}

	public boolean isHasReceiveSubscribeAnswer() {
		return hasReceiveSubscribeAnswer;
	}

	public boolean isSubscribeOk() {
		return isSubscribeOk;
	}

	public boolean hasReceiveUnsubscribeAnswer() {
		return hasReceiveUnsubscribeAnswer;
	}

	public boolean isUnsubscribeOk() {
		return isUnsubscribeOk;
	}

	public List<Summary> getSummariesSnapshot() {
		return summariesSnapshot;
	}

	public List<Execution> getExecutionsSnapshot() {
		return executionsSnapshot;
	}

	public List<Order> getOrdersSnapshot() {
		return ordersSnapshot;
	}

	public List<Instrument> getInstrumentsSnapshot() {
		return instrumentsSnapshot;
	}

	public boolean hasReceivedOrder() {
		return hasReceivedOrder;
	}

	public boolean isOrderOk() {
		return isOrderOk;
	}
}
