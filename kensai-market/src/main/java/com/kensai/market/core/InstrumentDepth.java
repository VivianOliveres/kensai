package com.kensai.market.core;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.google.common.base.Objects;
import com.kensai.market.IdGenerator;
import com.kensai.protocol.Trading.BuySell;
import com.kensai.protocol.Trading.CommandStatus;
import com.kensai.protocol.Trading.Execution;
import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.MarketStatus;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.OrderStatus;
import com.kensai.protocol.Trading.Summary;
import com.kensai.protocol.Trading.Summary.Builder;

public class InstrumentDepth {

	private final Instrument instrument;

	private List<DepthRow> buyDepths = newArrayList();
	private List<DepthRow> sellDepths = newArrayList();

	private List<Order> buyTerminatedOrders = newArrayList();
	private List<Order> sellTerminatedOrders = newArrayList();

	private List<Execution> allExecutions = newArrayList();

	private final double open;
	private final double close;
	private double last;

	private long lastUpdate;
	private MarketStatus marketStatus = MarketStatus.OPEN;

	public InstrumentDepth(Instrument instrument, double open, double close, double last) {
		this.instrument = instrument;
		this.open = open;
		this.close = close;
		this.last = last;
	}

	public InstrumentDepth(Summary summary) {
		this(summary.getInstrument(), summary.getOpen(), summary.getClose(), summary.getLast());
	}

	public Instrument getInstrument() {
		return instrument;
	}

	public double getOpen() {
		return open;
	}

	public double getClose() {
		return close;
	}

	public double getLast() {
		return last;
	}

	public Summary toSummary() {
		return toSummaryBuilder().build();
	}

	public Summary.Builder toSummaryBuilder() {
		Builder builder = Summary.newBuilder().setInstrument(instrument).setLast(last).setMarketStatus(marketStatus).setClose(close).setOpen(open)
			.setTimestamp(lastUpdate);

		// Buy side
		for (int i = 0; i < buyDepths.size(); i++) {
			DepthRow depth = buyDepths.get(i);
			com.kensai.protocol.Trading.Depth.Builder depthBuilder = depth.toDepthBuilder().setDepth(i);
			builder.addBuyDepths(depthBuilder);
		}

		// Sell side
		for (int i = 0; i < sellDepths.size(); i++) {
			DepthRow depth = sellDepths.get(i);
			com.kensai.protocol.Trading.Depth.Builder depthBuilder = depth.toDepthBuilder().setDepth(i);
			builder.addSellDepths(depthBuilder);
		}

		return builder;
	}

	private void checkOrder(Order order) throws IllegalArgumentException {
		if (order == null) {
			throw new IllegalArgumentException("Null order is forbiden");
		}

		Instrument instr = order.getInstrument();
		if (instr == null || !instr.equals(instrument)) {
			throw new IllegalArgumentException("Invalid instrument [" + instr == null ? null : instr.getIsin() + "] is forbiden for this depth["
				+ instrument.getIsin() + "]");
		}
	}

	public InsertionResult insert(Order order) throws IllegalArgumentException {
		// Check preconditions
		checkOrder(order);

		// Prepare order to insert
		Order orderToInsert = Order.newBuilder(order).setId(IdGenerator.generateId()).build();

		// Do stuff
		InsertionResult result = doInsert(orderToInsert);

		// Update executions list
		if (result.hasExecutions()) {
			allExecutions.addAll(result.getExecutions());
		}

		// Update terminated orders for already inserted orders
		if (result.hasExecutedOrders()) {
			for (Order executedOrder : result.getExecutedOrders()) {
				updateTerminatedOrder(executedOrder);
			}
		}

		// Update terminated orders for resulted Orders
		updateTerminatedOrder(result.getResultedOrder());

		// Update general fields
		updateGeneralFields(result);

		return result;
	}

	private void updateGeneralFields(InsertionResult result) {
		// lastUpdate is now
		if (result.getResultedOrder() != null) {
			lastUpdate = System.currentTimeMillis();
		}

		// Last is equal to price of last executed order
		if (result.hasExecutedOrders()) {
			List<Order> executedOrders = result.getExecutedOrders();
			Order lastExecutedOrder = executedOrders.get(executedOrders.size() - 1);
			last = lastExecutedOrder.getPrice();
		}
	}

	private void updateTerminatedOrder(Order order) {
		if (order.getOrderStatus().equals(OrderStatus.TERMINATED)) {
			if (order.getSide().equals(BuySell.BUY)) {
				buyTerminatedOrders.add(order);

			} else {
				sellTerminatedOrders.add(order);
			}
		}
	}

	private InsertionResult doInsert(Order orderToInsert) {
		// If there is no opposite order to match
		if (buyDepths.isEmpty() && orderToInsert.getSide().equals(BuySell.SELL)) {
			return doInsertInSameSide(orderToInsert);

		} else if (sellDepths.isEmpty() && orderToInsert.getSide().equals(BuySell.BUY)) {
			return doInsertInSameSide(orderToInsert);
		}

		// If this order could hit orders opposite orders
		if (!buyDepths.isEmpty() && buyDepths.get(0).couldMakeExecutions(orderToInsert)) {
			return doInsert(orderToInsert, buyDepths, sellDepths);

		} else if (!sellDepths.isEmpty() && sellDepths.get(0).couldMakeExecutions(orderToInsert)) {
			return doInsert(orderToInsert, sellDepths, buyDepths);

		} else {
			// This order matches nothing
			return doInsertInSameSide(orderToInsert);
		}
	}

	private InsertionResult doInsert(Order orderToInsert, List<DepthRow> depthToInsert, List<DepthRow> oppositeDepth) {
		InsertionResult result = new InsertionResult(orderToInsert, null, null);
		for (int i = 0; i < depthToInsert.size(); i++) {
			DepthRow depth = depthToInsert.get(i);
			Order order = result.getResultedOrder();
			if (depth.couldMakeExecutions(order)) {
				InsertionResult otherResult = depth.insert(order);
				result = result.mergeWith(otherResult);

				if (depth.isEmpty()) {
					depthToInsert.remove(i);
					i--;
				}

				if (result.getResultedOrder().getOrderStatus().equals(OrderStatus.TERMINATED)) {
					return result;
				}

			} else {
				oppositeDepth.add(0, new DepthRow(orderToInsert.getPrice(), order.getSide(), order));
				return result;
			}
		}

		return result;
	}

	private InsertionResult doInsertInSameSide(Order orderToInsert) {
		boolean isBuy = orderToInsert.getSide().equals(BuySell.BUY);
		List<DepthRow> depthToInsert = isBuy ? buyDepths : sellDepths;
		Order order = Order.newBuilder(orderToInsert).setCommandStatus(CommandStatus.ACK).build();

		if (depthToInsert.isEmpty()) {
			depthToInsert.add(new DepthRow(order.getPrice(), order.getSide(), order));
			return new InsertionResult(order, null, null);
		}

		for (int i = 0; i < depthToInsert.size(); i++) {
			DepthRow depthToTest = depthToInsert.get(i);
			if (depthToTest.getPrice() == order.getPrice()) {
				return depthToTest.insert(order);

			} else if (isBuy && depthToTest.getPrice() < order.getPrice()) {
				DepthRow depthRow = new DepthRow(order.getPrice(), order.getSide(), order);
				depthToInsert.add(i, depthRow);
				return new InsertionResult(order, null, null);

			} else if (!isBuy && depthToTest.getPrice() > order.getPrice()) {
				DepthRow depthRow = new DepthRow(order.getPrice(), order.getSide(), order);
				depthToInsert.add(i, depthRow);
				return new InsertionResult(order, null, null);
			}
		}

		// Insert at bottom
		DepthRow depthRow = new DepthRow(order.getPrice(), order.getSide(), order);
		depthToInsert.add(depthRow);
		return new InsertionResult(order, null, null);
	}

	public InsertionResult update(Order order) throws IllegalArgumentException {
		// Check preconditions
		checkOrder(order);

		// Do stuff
		InsertionResult result = doUpdate(order);

		// Update general fields
		updateGeneralFields(result);

		return result;
	}

	private InsertionResult doUpdate(Order order) {
		// Remove then add order
		doDelete(order);
		return doInsert(order);
	}

	public InsertionResult delete(Order order) throws IllegalArgumentException {
		// Check preconditions
		checkOrder(order);

		InsertionResult result = doDelete(order);
		if (order.getSide().equals(BuySell.BUY)) {
			buyTerminatedOrders.add(result.getResultedOrder());

		} else {
			sellTerminatedOrders.add(result.getResultedOrder());
		}

		// Update general fields
		updateGeneralFields(result);

		return result;
	}

	private InsertionResult doDelete(Order order) {
		if (order.getSide().equals(BuySell.BUY)) {
			if (contains(buyDepths, order)) {
				remove(buyDepths, order);
				Order resultedOrder = Order.newBuilder(order).setOrderStatus(OrderStatus.DELETED).setCommandStatus(CommandStatus.ACK).build();
				return new InsertionResult(resultedOrder, null, null);

			} else {
				Order resultedOrder = Order.newBuilder(order).setCommandStatus(CommandStatus.NACK).build();
				return new InsertionResult(resultedOrder, null, null);
			}

		} else {
			if (contains(sellDepths, order)) {
				remove(sellDepths, order);
				Order resultedOrder = Order.newBuilder(order).setOrderStatus(OrderStatus.DELETED).setCommandStatus(CommandStatus.ACK).build();
				return new InsertionResult(resultedOrder, null, null);

			} else {
				Order resultedOrder = Order.newBuilder(order).setCommandStatus(CommandStatus.NACK).build();
				return new InsertionResult(resultedOrder, null, null);
			}
		}
	}

	private boolean contains(List<DepthRow> depthRows, Order order) {
		for (DepthRow depthRow : depthRows) {
			if (depthRow.getPrice() != order.getPrice()) {
				continue;
			}

			return depthRow.contains(order);
		}

		return false;
	}

	private void remove(List<DepthRow> depthRows, Order order) {
		for (DepthRow depthRow : depthRows) {
			if (depthRow.getPrice() != order.getPrice()) {
				continue;
			}

			depthRow.remove(order);
			return;
		}
	}

	public List<Order> getAllOrders() {
		List<Order> orders = getAllActiveOrders();
		orders.addAll(buyTerminatedOrders);
		orders.addAll(sellTerminatedOrders);
		return orders;
	}

	public List<Order> getAllActiveOrders() {
		List<Order> orders = getAllOrders(buyDepths);
		orders.addAll(getAllOrders(sellDepths));
		return orders;
	}

	private List<Order> getAllOrders(List<DepthRow> depths) {
		List<Order> orders = newArrayList();
		for (DepthRow depth : depths) {
			orders.addAll(depth.getAllOrders());
		}

		return orders;
	}

	public List<Execution> getAllExecutions() {
		return newArrayList(allExecutions);
	}

	public boolean hasOrder(long orderId) {
		List<Order> allOrders = getAllOrders();
		for (Order order : allOrders) {
			if (order.getId() == orderId) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("instrument", instrument.getName())
			.add("last", last)
			.add("lastUpdate", lastUpdate)
			.toString();
	}

}
