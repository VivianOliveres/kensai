package com.kensai.market.factories;

import com.kensai.protocol.Trading.BuySell;
import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.OrderAction;
import com.kensai.protocol.Trading.OrderStatus;

public final class OrderFactory {

	public static final String USER_DATA = "user_data";
	public static final String USER = "user";

	public static final Instrument INSTRUMENT = Instrument.newBuilder().setIsin("isin").build();

	public final static Order.Builder create(int initialQty, int execQty, BuySell side) {
		return Order.newBuilder().setInitialQuantity(initialQty).setExecutedQuantity(execQty).setSide(side).setUserData(USER_DATA).setUser(USER)
			.setAction(OrderAction.INSERT).setInstrument(INSTRUMENT);
	}

	public final static Order.Builder create(int initialQty, int execQty, BuySell side, OrderStatus status) {
		return Order.newBuilder().setInitialQuantity(initialQty).setExecutedQuantity(execQty).setSide(side).setUserData(USER_DATA).setUser(USER)
			.setAction(OrderAction.INSERT).setInstrument(INSTRUMENT).setOrderStatus(status);
	}

	public final static Order.Builder create(double price, int initialQty, int execQty, BuySell side) {
		return Order.newBuilder().setInitialQuantity(initialQty).setExecutedQuantity(execQty).setSide(side).setPrice(price).setUserData(USER_DATA)
			.setUser(USER).setAction(OrderAction.INSERT).setInstrument(INSTRUMENT);
	}

	public final static Order.Builder create(double price, int initialQty, int execQty, BuySell side, OrderStatus status) {
		return Order.newBuilder().setInitialQuantity(initialQty).setExecutedQuantity(execQty).setSide(side).setPrice(price).setUserData(USER_DATA)
			.setUser(USER).setAction(OrderAction.INSERT).setInstrument(INSTRUMENT).setOrderStatus(status);
	}

	public final static Order.Builder create(double price, int initialQty, BuySell side) {
		return Order.newBuilder().setInitialQuantity(initialQty).setSide(side).setPrice(price).setUserData(USER_DATA).setUser(USER)
			.setAction(OrderAction.INSERT).setInstrument(INSTRUMENT);
	}

	public final static Order.Builder create(double price, int initialQty, BuySell side, OrderStatus status) {
		return Order.newBuilder().setInitialQuantity(initialQty).setSide(side).setPrice(price).setUserData(USER_DATA).setUser(USER)
			.setAction(OrderAction.INSERT).setInstrument(INSTRUMENT).setOrderStatus(status);
	}
}
