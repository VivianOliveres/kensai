package com.kensai.market.factories;

import static com.kensai.market.factories.DatasUtil.USER;

import com.kensai.market.IdGenerator;
import com.kensai.protocol.Trading.Execution;
import com.kensai.protocol.Trading.Order;

public final class ExecutionFactory {

	private ExecutionFactory() {
		// Can not be instanciated
	}

	public static Execution.Builder create(Order order) {
		double price = order.getPrice();
		int qty = order.getExecutedQuantity();
		long id = IdGenerator.generateId();
		long time = System.currentTimeMillis();
		return Execution.newBuilder().setOrder(order).setPrice(price).setQuantity(qty).setId(id).setTime(time).setUser(USER);
	}

}
