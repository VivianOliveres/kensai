package com.kensai.market.matchers;

import java.util.List;

import org.mockito.ArgumentMatcher;

import com.kensai.protocol.Trading.Messages;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.OrdersSnapshot;

public class OrdersSnapshotMatcher extends ArgumentMatcher {

	private List<Order> orders;

	public OrdersSnapshotMatcher() {
		this(null);
	}

	public OrdersSnapshotMatcher(List<Order> orders) {
		this.orders = orders;
	}

	@Override
	public boolean matches(Object argument) {
		if (argument instanceof OrdersSnapshot) {
			OrdersSnapshot cmd = (OrdersSnapshot) argument;
			return matchesOrders(cmd);

		} else if (argument instanceof Messages) {
			Messages msg = (Messages) argument;
			return msg.hasOrdersSnapshot() && matchesOrders(msg.getOrdersSnapshot());
		}

		return false;
	}

	private boolean matchesOrders(OrdersSnapshot snapshot) {
		if (orders == null) {
			return true;
		}

		return orders.size() == snapshot.getOrdersCount();
	}

}
