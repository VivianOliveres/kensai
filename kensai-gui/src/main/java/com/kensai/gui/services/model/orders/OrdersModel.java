package com.kensai.gui.services.model.orders;

import java.util.Iterator;
import java.util.List;

import javafx.collections.ObservableList;

import com.kensai.gui.services.model.instruments.InstrumentModel;
import com.kensai.protocol.Trading.Order;

public class OrdersModel implements Iterable<OrderModel> {
	public static final int DEFAULT_SIZE = 500;

	private final OrdersModelLimiter limiter;

	public OrdersModel() {
		this(new OrdersModelLimiter(DEFAULT_SIZE));
	}

	public OrdersModel(OrdersModelLimiter limiter) {
		this.limiter = limiter;
	}

	public ObservableList<OrderModel> getOrders() {
		return limiter.getOrders();
	}

	public boolean contains(Order order) {
		return limiter.getOrders().stream().filter(model -> model.equals(order)).findFirst().isPresent();
	}

	public boolean contains(OrderModel order) {
		return limiter.getOrders().contains(order);
	}

	public void add(Order order, InstrumentModel insrument) {
		limiter.add(order, insrument);
	}

	public void add(OrderModel order) {
		if (!contains(order)) {
			limiter.add(order);
		}
	}

	public List<OrderModel> getOrders(InstrumentModel instrument) {
		return limiter.getOrders(instrument);
	}

	@Override
	public Iterator<OrderModel> iterator() {
		return limiter.getOrders().iterator();
	}

	public int size() {
		return limiter.getOrders().size();
	}

}
