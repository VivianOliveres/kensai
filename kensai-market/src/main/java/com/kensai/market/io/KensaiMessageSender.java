package com.kensai.market.io;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.google.common.collect.Maps;
import com.kensai.protocol.Trading.CommandStatus;
import com.kensai.protocol.Trading.Execution;
import com.kensai.protocol.Trading.ExecutionsSnapshot;
import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.InstrumentsSnapshot;
import com.kensai.protocol.Trading.Messages;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.OrdersSnapshot;
import com.kensai.protocol.Trading.SubscribeCommand;
import com.kensai.protocol.Trading.SummariesSnapshot;
import com.kensai.protocol.Trading.Summary;
import com.kensai.protocol.Trading.UnsubscribeCommand;

public class KensaiMessageSender {
	private static final Logger log = LogManager.getLogger(KensaiMessageSender.class);

	private Map<String, Channel> users = Maps.newHashMap();

	public void addUser(String user, Channel channel) {
		users.put(user, channel);
	}

	public void removeUser(String user) {
		users.remove(user);
	}

	public boolean contains(String user) {
		if (user == null) {
			return false;
		}

		return users.containsKey(user);
	}

	public void sendNack(Order order, Channel channel, String errorMsg) {
		sendNack(Order.newBuilder(order), channel, errorMsg);
	}

	public void sendNack(Order.Builder builder, Channel channel, String errorMsg) {
		Order response = builder.setErrorMessage(errorMsg).setCommandStatus(CommandStatus.NACK).build();
		Messages msg = Messages.newBuilder().setOrder(response).build();
		channel.write(msg);
	}

	public void sendNack(UnsubscribeCommand cmd, Channel channel, String errorMsg) {
		sendNack(UnsubscribeCommand.newBuilder(cmd), channel, errorMsg);
	}

	public void sendNack(UnsubscribeCommand.Builder builder, Channel channel, String errorMsg) {
		UnsubscribeCommand response = builder.setErrorMessage(errorMsg).setStatus(CommandStatus.NACK).build();
		Messages msg = Messages.newBuilder().setUnsubscribeCommand(response).build();
		channel.write(msg);
	}

	public void sendNack(SubscribeCommand cmd, Channel channel, String errorMsg) {
		sendNack(SubscribeCommand.newBuilder(cmd), channel, errorMsg);
	}

	public void sendNack(SubscribeCommand.Builder builder, Channel channel, String errorMsg) {
		SubscribeCommand response = builder.setErrorMessage(errorMsg).setStatus(CommandStatus.NACK).build();
		Messages msg = Messages.newBuilder().setSubscribeCommand(response).build();
		channel.write(msg);
	}

	public void sendAck(Order order, Channel channel) {
		sendAck(Order.newBuilder(order), channel);
	}

	public void sendAck(Order.Builder builder, Channel channel) {
		Order response = builder.setLastUpdateTime(System.currentTimeMillis()).setCommandStatus(CommandStatus.ACK).build();
		Messages msg = Messages.newBuilder().setOrder(response).build();
		channel.write(msg);
	}

	public void sendAck(SubscribeCommand cmd, Channel channel) {
		sendAck(SubscribeCommand.newBuilder(cmd), channel);
	}

	public void sendAck(SubscribeCommand.Builder builder, Channel channel) {
		SubscribeCommand cmd = builder.setStatus(CommandStatus.ACK).build();
		Messages msg = Messages.newBuilder().setSubscribeCommand(cmd).build();
		channel.write(msg);
	}

	public void sendAck(UnsubscribeCommand cmd, Channel channel) {
		sendAck(UnsubscribeCommand.newBuilder(), channel);
	}

	public void sendAck(UnsubscribeCommand.Builder builder, Channel channel) {
		UnsubscribeCommand cmd = builder.setStatus(CommandStatus.ACK).build();
		Messages msg = Messages.newBuilder().setUnsubscribeCommand(cmd).build();
		channel.write(msg);
	}

	public void sendInstrumentsSnapshot(String user, List<Instrument> instruments) {
		if (!users.containsKey(user)) {
			log.error("Can not send ExecutionsSnapshot - cause: unknow user [{}]", user);
			return;
		}

		Channel channel = users.get(user);
		InstrumentsSnapshot snapshot = InstrumentsSnapshot.newBuilder().addAllInstruments(instruments).build();
		Messages msg = Messages.newBuilder().setInstrumentsSnapshot(snapshot).build();
		channel.write(msg);
	}

	public void sendSummariesSnapshot(String user, List<Summary> summaries) {
		if (!users.containsKey(user)) {
			log.error("Can not send ExecutionsSnapshot - cause: unknow user [{}]", user);
			return;
		}

		Channel channel = users.get(user);
		SummariesSnapshot snapshot = SummariesSnapshot.newBuilder().addAllSummaries(summaries).build();
		Messages msg = Messages.newBuilder().setSummariesSnapshot(snapshot).build();
		channel.write(msg);
	}

	public void sendOrdersSnapshot(String user, List<Order> orders) {
		if (!users.containsKey(user)) {
			log.error("Can not send ExecutionsSnapshot - cause: unknow user [{}]", user);
			return;
		}

		Channel channel = users.get(user);
		OrdersSnapshot snapshot = OrdersSnapshot.newBuilder().addAllOrders(orders).build();
		Messages msg = Messages.newBuilder().setOrdersSnapshot(snapshot).build();
		channel.write(msg);
	}

	public void sendExecutionsSnapshot(String user, List<Execution> executions) {
		if (!users.containsKey(user)) {
			log.error("Can not send ExecutionsSnapshot - cause: unknow user [{}]", user);
			return;
		}

		Channel channel = users.get(user);
		ExecutionsSnapshot snapshot = ExecutionsSnapshot.newBuilder().addAllExecutions(executions).build();
		Messages msg = Messages.newBuilder().setExecutionsSnapshot(snapshot).build();
		channel.write(msg);
	}

	public void send(Order order, Channel channel) {
		Messages msg = Messages.newBuilder().setOrder(order).build();
		channel.write(msg);
	}

	public void send(Execution exec, Channel channel) {
		Messages msg = Messages.newBuilder().setExecution(exec).build();
		channel.write(msg);
	}

	public void send(Summary summary) {
		Messages msg = Messages.newBuilder().setSummary(summary).build();
		users.values().forEach(channel -> channel.write(msg));
	}

}
