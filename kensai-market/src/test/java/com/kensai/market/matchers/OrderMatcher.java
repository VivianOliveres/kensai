package com.kensai.market.matchers;

import org.mockito.ArgumentMatcher;

import com.kensai.protocol.Trading.Messages;
import com.kensai.protocol.Trading.Order;

public class OrderMatcher extends ArgumentMatcher {

	private Order order;

	public OrderMatcher() {
		this(null);
	}

	public OrderMatcher(Order order) {
		this.order = order;
	}

	@Override
	public boolean matches(Object argument) {
		if (argument instanceof Order) {
			Order order = (Order) argument;
			return matchesOrder(order);

		} else if (argument instanceof Messages) {
			Messages msg = (Messages) argument;
			return msg.hasOrder() && matchesOrder(msg.getOrder());
		}

		return false;
	}

	private boolean matchesOrder(Order otherOrder) {
		if (order == null) {
			return true;
		}

		return order.equals(otherOrder);
	}

}
