package com.kensai.market.core;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.List;

import com.kensai.market.IdGenerator;
import com.kensai.protocol.Trading.BuySell;
import com.kensai.protocol.Trading.Depth;
import com.kensai.protocol.Trading.Execution;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.OrderStatus;

public class DepthRow implements Comparable<DepthRow> {

	private double price;
	private final BuySell side;

	private List<Order> orders = newArrayList();

	public DepthRow(double price, BuySell side, Order... orders) {
		this(price, side, Arrays.asList(orders));
	}

	public DepthRow(double price, BuySell side, List<Order> orders) {
		this.price = price;
		this.side = side;

		if (orders != null) {
			this.orders.addAll(orders);
		}
	}

	public Depth.Builder toDepthBuilder() {
		int qty = 0;
		for (Order order : orders) {
			qty += order.getInitialQuantity() - order.getExecutedQuantity();
		}

		return Depth.newBuilder().setPrice(price).setQuantity(qty);
	}

	public boolean isEmpty() {
		return orders.isEmpty();
	}

	public int size() {
		return orders.size();
	}

	public List<Order> getOrders() {
		return newArrayList(orders);
	}

	public Order getOrderAt(int index) {
		return orders.get(index);
	}

	public double getPrice() {
		return price;
	}

	public boolean couldMakeExecutions(Order order) {
		if (order.getSide().equals(side)) {
			return false;
		}

		if (side.equals(BuySell.BUY)) {
			return price >= order.getPrice();

		} else {
			return price <= order.getPrice();
		}
	}

	public InsertionResult insert(Order order) throws IllegalArgumentException {
		if (order == null || order.getPrice() != price) {
			throw new IllegalArgumentException("Invalid order [" + order == null ? null : order.getPrice() + "] for this depth[price=" + price + "]");
		}

		if (order.getSide().equals(side)) {
			return insertInSameSide(order);

		} else {
			return insertInOppositeSide(order);
		}
	}

	private InsertionResult insertInSameSide(Order order) {
		Order insertedOrder = Order.newBuilder(order).setOrderStatus(OrderStatus.ON_MARKET).build();
		orders.add(insertedOrder);
		return new InsertionResult(insertedOrder, null, null);
	}

	private InsertionResult insertInOppositeSide(Order order) {
		// TODO Auto-generated method stub
		List<Order> executedOrders = newArrayList();
		List<Execution> executions = newArrayList();
		if (!couldMakeExecutions(order)) {
			return new InsertionResult(order, executedOrders, executions);
		}

		long now = System.currentTimeMillis();
		List<Order> newDepthRow = newArrayList();
		int remainingQty = order.getInitialQuantity() - order.getExecutedQuantity();
		for (Order oppositeOrder : orders) {
			if (remainingQty <= 0) {
				newDepthRow.add(oppositeOrder);
				continue;
			}

			int oppositeRemainingQty = oppositeOrder.getInitialQuantity() - oppositeOrder.getExecutedQuantity();
			if (remainingQty <= oppositeRemainingQty) {
				// CASE: Full execution of inserted order

				// Build new order in depth
				int newOppositeExecQty = oppositeOrder.getExecutedQuantity() + remainingQty;
				Order newOrderRow = Order.newBuilder(oppositeOrder).setExecutedQuantity(newOppositeExecQty).setLastUpdateTime(now).build();
				newDepthRow.add(newOrderRow);
				executedOrders.add(newOrderRow);

				// Build an execution for depth order
				executions.add(Execution.newBuilder().setOrder(oppositeOrder).setPrice(price).setQuantity(remainingQty).setTime(now)
					.setUser(order.getUser()).setId(IdGenerator.generateId()).build());

				// Build an execution for ResultedOrder
				executions.add(Execution.newBuilder().setId(IdGenerator.generateId()).setOrder(order).setPrice(price).setQuantity(remainingQty)
					.setTime(now).setUser(order.getUser()).build());

				// Update remainingQty
				remainingQty = 0;

			} else {
				// CASE: Full execution of OppositeOrder
				remainingQty = remainingQty - oppositeRemainingQty;

				// Build an execution for this order
				executions.add(Execution.newBuilder().setId(IdGenerator.generateId()).setOrder(order).setPrice(price).setQuantity(oppositeRemainingQty)
					.setTime(now).setUser(order.getUser()).build());

				// OppositeOrder will become fully executed
				Order fullyExecutedOrder = Order.newBuilder(oppositeOrder).setOrderStatus(OrderStatus.TERMINATED)
					.setExecutedQuantity(oppositeOrder.getInitialQuantity()).setLastUpdateTime(now).build();
				executedOrders.add(fullyExecutedOrder);
				executions.add(Execution.newBuilder().setId(IdGenerator.generateId()).setOrder(fullyExecutedOrder).setPrice(price)
					.setQuantity(oppositeRemainingQty).setTime(now).setUser(order.getUser()).build());
			}

		}

		// Update depth
		orders.clear();
		orders.addAll(newDepthRow);

		// Build resultedOrder
		Order.Builder resultedOrder = Order.newBuilder(order).setLastUpdateTime(now).setExecutedQuantity(order.getInitialQuantity() - remainingQty);
		if (remainingQty <= 0) {
			resultedOrder.setOrderStatus(OrderStatus.TERMINATED);

		} else {
			resultedOrder.setOrderStatus(OrderStatus.ON_MARKET);
		}

		return new InsertionResult(resultedOrder.build(), executedOrders, executions);
	}

	@Override
	public int compareTo(DepthRow other) {
		if (!side.equals(other.side)) {
			throw new RuntimeException("Comparing thow EnhancedDepth from different side is not allowed");
		}

		if (price == other.price) {
			return 0;
		}

		if (price < other.price) {
			return side.equals(BuySell.BUY) ? 1 : -1;

		} else {
			return side.equals(BuySell.BUY) ? -1 : 1;
		}
	}

}
