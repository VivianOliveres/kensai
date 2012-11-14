package com.kensai.market.core;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kensai.market.IdGenerator;
import com.kensai.market.io.KensaiMessageSender;
import com.kensai.protocol.Trading.Execution;
import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.OrderStatus;
import com.kensai.protocol.Trading.SubscribeCommand;
import com.kensai.protocol.Trading.Summary;
import com.kensai.protocol.Trading.UnsubscribeCommand;

public class KensaiMarket {

	private static final Logger log = LoggerFactory.getLogger(KensaiMarket.class);

	private final KensaiMessageSender sender;

	private final List<Instrument> instruments = Lists.newArrayList();
	private final Map<Instrument, Summary> summariesByInstrument = Maps.newHashMap();

	private final Map<Long, Order> orders = Maps.newHashMap();
	private final ListMultimap<Long, Execution> executionsByOrder = ArrayListMultimap.create();

	public KensaiMarket(Map<Instrument, Summary> summariesByInstrument, KensaiMessageSender sender) {
		this.instruments.addAll(summariesByInstrument.keySet());
		this.summariesByInstrument.putAll(summariesByInstrument);
		this.sender = sender;
	}

	public void doSubscribeCommand(SubscribeCommand cmd, Channel channel) {
		// Preconditions
		String user = cmd.getUser();
		if (!sender.isValidUser(user)) {
			sender.sendNack(cmd, channel, "Invalid user [" + user + "]");
			return;
		}

		// Save user and send ACK
		sender.addUser(user, channel);
		sender.sendAck(cmd, channel);

		// Send snapshots
		sender.sendInstrumentsSnapshot(user, newArrayList(instruments));
		sender.sendSummariesSnapshot(user, newArrayList(summariesByInstrument.values()));
		sender.sendOrdersSnapshot(user, newArrayList(orders.values()));
		sender.sendExecutionsSnapshot(user, newArrayList(executionsByOrder.values()));
	}

	public void doUnsubscribeCommand(UnsubscribeCommand cmd, Channel channel) {
		// Preconditions
		String user = cmd.getUser();
		if (!sender.isValidUser(user)) {
			sender.sendNack(cmd, channel, "Invalid user [" + user + "]");
			return;
		}

		// Remove user and send ACK
		sender.removeUser(user);
		sender.sendAck(cmd, channel);
	}

	public void doOrder(Order order, Channel channel) {
		// Check user credentials
		String user = order.getUser();
		if (!sender.isValidUser(user)) {
			String errorMesg = "User [" + user + "] does not have credentials to manage orders";
			log.info(errorMesg);
			sender.sendNack(order, channel, errorMesg);
			return;
		}

		// Manage order
		switch (order.getAction()) {
		case DELETE:
			doDeleteOrder(order, channel);
			break;

		case UPDATE:
			doUpdateOrder(order, channel);
			break;

		case INSERT:
			doInsertOrder(order, channel);
			break;

		default:
			String errorMesg = "Unknow OrderAction[" + order.getAction() + "]";
			log.warn(errorMesg);
			sender.sendNack(order, channel, errorMesg);
			return;
		}
	}

	private boolean isValidOrderId(long id) {
		return orders.containsKey(id);
	}

	private void doDeleteOrder(Order order, Channel channel) {
		if (!isValidOrderId(order.getId())) {
			String errorMesg = "Invalid orderId[" + order.getId() + "]";
			log.error(errorMesg);
			sender.sendNack(order, channel, errorMesg);
			return;
		}

		// Update order status and send notification
		Order.Builder orderBuilder = Order.newBuilder(order).setOrderStatus(OrderStatus.DELETED);
		sender.sendAck(orderBuilder, channel);
	}

	private void doUpdateOrder(Order order, Channel channel) {
		if (!isValidOrderId(order.getId())) {
			String errorMesg = "Invalid orderId[" + order.getId() + "]";
			log.error(errorMesg);
			sender.sendNack(order, channel, errorMesg);
			return;
		}

		// TODO Auto-generated method stub

	}

	private void doInsertOrder(Order order, Channel channel) {
		// TODO Auto-generated method stub

		// Add identifier
		Order.Builder orderBuilder = Order.newBuilder(order).setId(IdGenerator.generateId());
	}

}
