package com.kensai.market.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.kensai.market.core.UserCredentials;
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
import com.kensai.protocol.Trading.User;

public class KensaiMessageSender {
	private static final Logger log = LogManager.getLogger(KensaiMessageSender.class);

	private List<UserCredentials> users = new ArrayList<>();

	public void addUser(UserCredentials uc, SubscribeCommand cmd) {
		if (users.contains(uc)) {
			sendNack(cmd, uc.getChannel(), "User [" + uc.getName() + "] already subscribed");

		} else {
			users.add(uc);
			sendAck(cmd, uc.getChannel());
		}
	}

	public void addUser(User user, Channel channel, SubscribeCommand cmd) {
		addUser(new UserCredentials(user, channel), cmd);
	}

	public void removeUser(UserCredentials uc, UnsubscribeCommand cmd) {
		if (users.contains(uc)) {
			users.remove(uc);
			sendAck(cmd, uc.getChannel());

		} else {
			sendNack(cmd, uc.getChannel(), "Can not unsubscribe User [" + uc.getName() + "] - Reason: user does not exist");
		}
	}

	public void removeUser(User user, UnsubscribeCommand cmd, Channel channel) {
		removeUser(new UserCredentials(user, channel), cmd);
	}

	public boolean contains(UserCredentials user) {
		return contains(user.getUser());
	}

	public boolean contains(User user) {
		Optional<UserCredentials> optional = users.stream().filter(bean -> bean.getUser().equals(user)).findFirst();
		return optional.isPresent();
	}

	public UserCredentials getUser(User user) {
		Optional<UserCredentials> optional = users.stream().filter(bean -> bean.getUser().equals(user)).findFirst();
		if (optional.isPresent()) {
			return optional.get();

		} else {
			return null;
		}
	}

	public UserCredentials getUser(UserCredentials user) {
		Optional<UserCredentials> optional = users.stream().filter(bean -> bean.equals(user)).findFirst();
		if (optional.isPresent()) {
			return optional.get();

		} else {
			return null;
		}
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

	public void sendAck(SubscribeCommand cmd, Channel channel) {
		sendAck(SubscribeCommand.newBuilder(cmd), channel);
	}

	public void sendAck(SubscribeCommand.Builder builder, Channel channel) {
		SubscribeCommand cmd = builder.setStatus(CommandStatus.ACK).build();
		Messages msg = Messages.newBuilder().setSubscribeCommand(cmd).build();
		channel.write(msg);
	}

	public void sendAck(UnsubscribeCommand cmd, Channel channel) {
		sendAck(UnsubscribeCommand.newBuilder(cmd), channel);
	}

	public void sendAck(UnsubscribeCommand.Builder builder, Channel channel) {
		UnsubscribeCommand cmd = builder.setStatus(CommandStatus.ACK).build();
		Messages msg = Messages.newBuilder().setUnsubscribeCommand(cmd).build();
		channel.write(msg);
	}

	public void sendInstrumentsSnapshot(User user, List<Instrument> instruments) {
		if (!contains(user)) {
			log.error("Can not send ExecutionsSnapshot - cause: unknow user [{}]", user);
			return;
		}

		InstrumentsSnapshot snapshot = InstrumentsSnapshot.newBuilder().addAllInstruments(instruments).build();
		Messages msg = Messages.newBuilder().setInstrumentsSnapshot(snapshot).build();
		getUser(user).getChannel().write(msg);
	}

	public void sendSummariesSnapshot(User user, List<Summary> summaries) {
		if (!contains(user)) {
			log.error("Can not send SummariesSnapshot - cause: unknow user [{}]", user);
			return;
		}

		SummariesSnapshot.Builder builder = SummariesSnapshot.newBuilder();
		UserCredentials uc = getUser(user);
		if (uc.isListeningSummary()) {
			builder.addAllSummaries(summaries);
		}

		SummariesSnapshot snapshot = builder.build();
		Messages msg = Messages.newBuilder().setSummariesSnapshot(snapshot).build();
		uc.getChannel().write(msg);
	}

	public void sendOrdersSnapshot(User user, List<Order> orders) {
		if (!contains(user)) {
			log.error("Can not send OrdersSnapshot - cause: unknow user [{}]", user);
			return;
		}

		UserCredentials uc = getUser(user);
		List<Order> ordersForUser = orders.stream().filter(order -> uc.isListeningOrderFrom(order)).collect(Collectors.toList());
		OrdersSnapshot snapshot = OrdersSnapshot.newBuilder().addAllOrders(ordersForUser).build();
		Messages msg = Messages.newBuilder().setOrdersSnapshot(snapshot).build();
		uc.getChannel().write(msg);
	}

	public void sendExecutionsSnapshot(User user, List<Execution> executions) {
		if (!contains(user)) {
			log.error("Can not send ExecutionsSnapshot - cause: unknow user [{}]", user);
			return;
		}

		UserCredentials uc = getUser(user);
		List<Execution> executionsForUser = executions.stream().filter(exec -> uc.isListeningExecFrom(exec)).collect(Collectors.toList());
		ExecutionsSnapshot snapshot = ExecutionsSnapshot.newBuilder().addAllExecutions(executionsForUser).build();
		Messages msg = Messages.newBuilder().setExecutionsSnapshot(snapshot).build();
		uc.getChannel().write(msg);
	}

	public void send(Order order) {
		Messages msg = Messages.newBuilder().setOrder(order).build();
		users.stream().filter(user -> user.isListeningOrderFrom(order.getUser())).forEach(user -> user.getChannel().write(msg));
	}

	public void send(Execution exec) {
		Messages msg = Messages.newBuilder().setExecution(exec).build();
		users.stream().filter(user -> user.isListeningExecFrom(exec.getUser())).forEach(user -> user.getChannel().write(msg));
	}

	public void send(Summary summary) {
		Messages msg = Messages.newBuilder().setSummary(summary).build();
		users.stream().filter(user -> user.isListeningSummary()).forEach(user -> user.getChannel().write(msg));
	}

}
