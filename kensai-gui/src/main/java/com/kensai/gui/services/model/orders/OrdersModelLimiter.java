package com.kensai.gui.services.model.orders;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.kensai.gui.services.model.instruments.InstrumentModel;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.OrderStatus;

public class OrdersModelLimiter {

	private final ObservableList<OrderModel> orders = FXCollections.observableArrayList();

	private final int size;

	public OrdersModelLimiter(int size) {
		this.size = size;
	}

	public boolean contains(Order order) {
		return orders.stream().filter(model -> model.equals(order)).findFirst().isPresent();
	}

	public boolean contains(OrderModel order) {
		return orders.contains(order);
	}

	public void add(Order order, InstrumentModel insrument) {
		if (contains(order)) {
			getOrders(insrument).stream().filter(model -> model.equals(order)).findFirst().get().update(order);

		} else {
			orders.add(0, new OrderModel(order, insrument));
			clean();
		}
	}

	private void clean() {
		if (orders.size() <= size) {
			return;
		}

		// Remove firstly TERMINATED or DELETED orders
		for (int i = orders.size() - 1; i >= 0; i--) {
			OrderModel order = orders.get(i);
			if (order.getStatus().equals(OrderStatus.ON_MARKET)) {
				continue;
			}

			// Delete it
			orders.remove(i);

			// Continue if needed
			if (orders.size() <= size) {
				return;
			}
		}

		// THEN: remove all firstly orders added
		while (orders.size() > size) {
			orders.remove(orders.size() - 1);
		}
	}

	public void add(OrderModel order) {
		if (!contains(order)) {
			orders.add(0, order);
			clean();
		}
	}

	public List<OrderModel> getOrders(InstrumentModel instrument) {
		return orders.filtered(model -> model.getInstrument().equals(instrument));
	}

	public ObservableList<OrderModel> getOrders() {
		return orders;
	}

	public int size() {
		return orders.size();
	}

}
