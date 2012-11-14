package com.kensai.market.core;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.kensai.protocol.Trading.Execution;
import com.kensai.protocol.Trading.Order;

public class InsertionResult {

	private final Order resultedOrder;
	private final List<Order> executedOrders = newArrayList();
	private final List<Execution> executions = newArrayList();

	public InsertionResult(Order resultedOrder, List<Order> executedOrders, List<Execution> executions) {
		this.resultedOrder = resultedOrder;

		if (executedOrders != null) {
			this.executedOrders.addAll(executedOrders);
		}

		if (executions != null) {
			this.executions.addAll(executions);
		}
	}

	public boolean hasExecutedOrders() {
		return !executedOrders.isEmpty();
	}

	public List<Order> getExecutedOrders() {
		return executedOrders;
	}

	public boolean hasExecutions() {
		return !executions.isEmpty();
	}

	public List<Execution> getExecutions() {
		return executions;
	}

	public Order getResultedOrder() {
		return resultedOrder;
	}

}
