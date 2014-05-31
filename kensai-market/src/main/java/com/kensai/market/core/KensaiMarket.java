package com.kensai.market.core;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.google.common.collect.Lists;
import com.kensai.market.io.KensaiMessageSender;
import com.kensai.protocol.Trading.Execution;
import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.SubscribeCommand;
import com.kensai.protocol.Trading.Summary;
import com.kensai.protocol.Trading.UnsubscribeCommand;

public class KensaiMarket {

	private static final Logger log = LogManager.getLogger(KensaiMarket.class);

	private final KensaiMessageSender sender;

	private final List<InstrumentDepth> depths = Lists.newArrayList();

	public KensaiMarket(KensaiMessageSender sender, List<InstrumentDepth> depths) {
		this.sender = sender;
		this.depths.addAll(depths);
	}

	public void receivedSubscribe(SubscribeCommand cmd, Channel channel) {
		// Precondition: command not null
		if (cmd == null) {
			log.error("Invalid SubscribeCommand[{}]", cmd);
			return;
		}

		// If user is already subscribed, send NACK
		String user = cmd.getUser();
		if (sender.contains(user)) {
			sender.sendNack(cmd, channel, "User [" + user + "] already subscribed");
		} else {
			sender.sendAck(cmd, channel);
		}

		// Save user
		sender.addUser(user, channel);

		// Send snapshots
		sender.sendInstrumentsSnapshot(user, getInstruments());
		sender.sendSummariesSnapshot(user, getSummariesSnapshot());
		sender.sendOrdersSnapshot(user, getOrdersSnapshot(user));
		sender.sendExecutionsSnapshot(user, getExecutionsSnapshot(user));
	}

	public List<Summary> getSummariesSnapshot() {
		List<Summary> summaries = newArrayList();
		for (InstrumentDepth depth : depths) {
			Summary summary = depth.toSummary();
			if (summary == null) {
				continue;
			}

			summaries.add(summary);
		}

		return summaries;
	}

	public List<Instrument> getInstruments() {
		List<Instrument> instruments = newArrayList();
		for (InstrumentDepth depth : depths) {
			instruments.add(depth.getInstrument());
		}

		return instruments;
	}

	public List<Order> getOrdersSnapshot(String user) {
		List<Order> orders = newArrayList();
		for (InstrumentDepth depth : depths) {
			orders.addAll(depth.getAllOrders(user));
		}

		return orders;
	}

	public List<Execution> getExecutionsSnapshot(String user) {
		List<Execution> executions = newArrayList();
		for (InstrumentDepth depth : depths) {
			executions.addAll(depth.getAllExecutions(user));
		}

		return executions;
	}

	public void receivedUnsubscribed(UnsubscribeCommand cmd, Channel channel) {
		// Precondition: command not null
		if (cmd == null) {
			log.error("Invalid UnsubscribeCommand [{}]", cmd);
			return;
		}

		// If user already subscribed, send NACK
		String user = cmd.getUser();
		if (!sender.contains(user)) {
			sender.sendNack(cmd, channel, "Invalid or unknow user [" + user + "]");
			return;
		}

		// Remove user and send ACK
		sender.removeUser(user);
		sender.sendAck(cmd, channel);
	}

	public void receivedOrder(Order order, Channel channel) {
		// Precondition: order is not null
		if (order == null) {
			log.error("Receive an invalid order [{}]", order);
			return;
		}

		// Check user credentials
		String user = order.getUser();
		if (!sender.contains(user)) {
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
			String errorMsg = "Unknow OrderAction[" + order.getAction() + "]";
			log.warn(errorMsg);
			sender.sendNack(order, channel, errorMsg);
			return;
		}
	}

	private boolean isValidOrderId(long id) {
		for (InstrumentDepth depth : depths) {
			if (depth.hasOrder(id)) {
				return true;
			}
		}

		return false;
	}

	private void doDeleteOrder(Order order, Channel channel) {
		// Check if this order exist
		if (!isValidOrderId(order.getId())) {
			String errorMesg = "Unknow orderId[" + order.getId() + "]";
			log.error(errorMesg);
			sender.sendNack(order, channel, errorMesg);
			return;
		}

		// Update order status and send notification
		InstrumentDepth depth = getDepth(order.getInstrument());
		InsertionResult result = depth.delete(order);
		manageResult(result, channel);
	}

	private InstrumentDepth getDepth(Instrument instrument) {
		for (InstrumentDepth depth : depths) {
			if (depth.getInstrument().equals(instrument)) {
				return depth;
			}
		}

		return null;
	}

	private void manageResult(InsertionResult result, Channel channel) {
		// Send resulted order
		sender.send(result.getResultedOrder(), channel);

		// Send executed orders
		if (result.hasExecutedOrders()) {
			for (Order execOrder : result.getExecutedOrders()) {
				sender.send(execOrder, channel);
			}
		}

		// Send executions
		if (result.hasExecutions()) {
			for (Execution exec : result.getExecutions()) {
				sender.send(exec, channel);
			}
		}

		// Send summary update
		Instrument instrument = result.getResultedOrder().getInstrument();
		InstrumentDepth depth = getDepth(instrument);
		Summary summary = depth.toSummary();
		if (summary.getBuyDepthsCount() > 0 && summary.getBuyDepths(0).getQuantity() > 0 && summary.getBuyDepths(0).getPrice() <= 0) {
			log.error("BuyDepth invalid");
			System.exit(0);
		}
		if (summary.getSellDepthsCount() > 0 && summary.getSellDepths(0).getQuantity() > 0 && summary.getSellDepths(0).getPrice() <= 0) {
			log.error("SellDepth invalid");
			System.exit(0);
		}
		sender.send(summary);
	}

	private void doUpdateOrder(Order order, Channel channel) {
		if (!isValidOrderId(order.getId())) {
			String errorMesg = "Invalid orderId[" + order.getId() + "]";
			log.error(errorMesg);
			sender.sendNack(order, channel, errorMesg);
			return;
		}

		// Update order status and send notification
		InstrumentDepth depth = getDepth(order.getInstrument());
		InsertionResult result = depth.update(order);
		manageResult(result, channel);
	}

	private void doInsertOrder(Order order, Channel channel) {
		// Check if this order has not been already inserted
		if (order.getId() > 0 && isValidOrderId(order.getId())) {
			String errorMesg = "Can not insert an already inserted order: orderId[" + order.getId() + "]";
			log.error(errorMesg);
			sender.sendNack(order, channel, errorMesg);
			return;
		}

		// Insert order and send notification
		InstrumentDepth depth = getDepth(order.getInstrument());
		InsertionResult result = depth.insert(order);
		manageResult(result, channel);
	}

}
