package com.kensai.gui.services.model.orders;

import java.util.Iterator;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.kensai.gui.services.model.instruments.InstrumentModel;
import com.kensai.protocol.Trading.Order;

public class OrdersModel implements Iterable<OrderModel> {

	private final ObservableList<OrderModel> orders = FXCollections.observableArrayList();

	public ObservableList<OrderModel> getOrders() {
		return orders;
	}

	public boolean contains(Order order) {
		return orders.stream().filter(model -> model.equals(order)).findFirst().isPresent();
	}

	public boolean contains(OrderModel order) {
		return orders.contains(order);
	}

	public void add(Order order, InstrumentModel insrument) {
		if (!contains(order)) {
			orders.add(new OrderModel(order, insrument));
		}
	}

	public void add(OrderModel order) {
		if (!contains(order)) {
			orders.add(order);
		}
	}

	public List<OrderModel> getOrders(InstrumentModel instrument) {
		return orders.filtered(model -> model.getInstrument().equals(instrument));
	}

	@Override
	public Iterator<OrderModel> iterator() {
		return orders.iterator();
	}

	public int size() {
		return orders.size();
	}

}
