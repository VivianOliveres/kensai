package com.kensai.market.core;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.kensai.protocol.Trading.User;

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
		User user = cmd.getUser();

		// Save user and send ACK/NAK
		sender.addUser(user, channel, cmd);

		// Send snapshots
		sender.sendInstrumentsSnapshot(user, getInstruments());
		sender.sendSummariesSnapshot(user, getSummariesSnapshot());
		sender.sendOrdersSnapshot(user, getOrdersSnapshot());
		sender.sendExecutionsSnapshot(user, getExecutionsSnapshot());
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
		return depths.stream().map(depth -> depth.getInstrument()).collect(Collectors.toList());
	}

	public List<Order> getOrdersSnapshot() {
		return depths.stream().flatMap(depth -> depth.getAllOrders().stream()).collect(Collectors.toList());
	}

	public List<Execution> getExecutionsSnapshot() {
		return depths.stream().flatMap(depth -> depth.getAllExecutions().stream()).collect(Collectors.toList());
	}

	public void receivedUnsubscribed(UnsubscribeCommand cmd, Channel channel) {
		// Precondition: command not null
		if (cmd == null) {
			log.error("Invalid UnsubscribeCommand [{}]", cmd);
			return;
		}

		// Remove user and send ACK/NACK
		sender.removeUser(cmd.getUser(), cmd, channel);
	}

	public void receivedOrder(Order order, Channel channel) {
		// Precondition: order is not null
		if (order == null) {
			log.error("Receive an invalid order [{}]", order);
			return;
		}

		// Precondition: order price and quantity are positive
		if (order.getPrice() <= 0) {
			String errorMsg = "Orders with zero or negative prices are invalid";
			log.info(errorMsg);
			sender.sendNack(order, channel, errorMsg);
			return;
		} else if (order.getInitialQuantity() <= 0) {
			String errorMesg = "Orders with zero or negative quantity are invalid";
			log.info(errorMesg);
			sender.sendNack(order, channel, errorMesg);
			return;
		}

		// Check user credentials
		User user = order.getUser();
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
		Optional<InstrumentDepth> optional = depths.stream().filter(depth -> depth.getInstrument().equals(instrument)).findFirst();
		if (optional.isPresent()) {
			return optional.get();

		} else {
			List<String> instruments = depths.stream().map(depth -> depth.getInstrument().getName() + "[" + depth.getInstrument().getIsin() + "]").collect(toList());
			log.error("getDepth: Can not find any Depth for {}[{}] into: {}", instrument.getName(), instrument.getIsin(), instruments);
			return null;
		}
	}

	private void manageResult(InsertionResult result, Channel channel) {
		// Send resulted order
		sender.send(result.getResultedOrder());

		// Send executed orders
		if (result.hasExecutedOrders()) {
			for (Order execOrder : result.getExecutedOrders()) {
				sender.send(execOrder);
			}
		}

		// Send executions
		if (result.hasExecutions()) {
			result.getExecutions().forEach(exec -> sender.send(exec));
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
