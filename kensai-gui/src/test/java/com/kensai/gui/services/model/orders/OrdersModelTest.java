package com.kensai.gui.services.model.orders;

import static com.kensai.gui.services.connectors.MarketConnector.DEFAULT_USER;
import static com.kensai.protocol.Trading.InstrumentType.STOCK;
import static com.kensai.protocol.Trading.OrderAction.INSERT;
import static com.kensai.protocol.Trading.OrderStatus.ON_MARKET;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.kensai.gui.services.model.instruments.InstrumentModel;
import com.kensai.protocol.Trading.BuySell;
import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.Order;

public class OrdersModelTest {

	private static final InstrumentModel ACCOR = new InstrumentModel("FR0000120404", "Accor", "CAC", "description", "STOCK", "CAC");
	private static final OrderModel ORDER_ACCOR = new OrderModel(ACCOR, 123, BuySell.SELL, 456.789, 159);

	private OrdersModel orders;

	@Before
	public void init() {
		orders = new OrdersModel();
	}

	@Test
	public void should_contains_nothing_at_beggining() {
		assertThat(orders).isEmpty();
		assertThat(orders.size()).isZero();
	}

	@Test
	public void should_add_OrderModel_and_contains_ok() {
		// WHEN: add an order
		orders.add(ORDER_ACCOR);

		// THEN: size is ok
		assertThat(orders).hasSize(1);

		// AND: contains is ok
		assertThat(orders.contains(ORDER_ACCOR)).isTrue();
	}

	@Test
	public void should_add_Order_and_contains_ok() {
		// GIVEN: order
		Instrument instrument = Instrument.newBuilder().setIsin("isin").setName("name").setDescription("description").setType(STOCK).build();
		InstrumentModel instrumentModel = new InstrumentModel(instrument, "CAC");
		Order order = Order.newBuilder().setId(123).setSide(BuySell.BUY).setInstrument(instrument).setAction(INSERT).setOrderStatus(ON_MARKET)
			.setPrice(456.789).setExecPrice(456.789).setInitialQuantity(159).setExecutedQuantity(15).setUserData("blabla").setUser(DEFAULT_USER)
			.setInsertTime(System.currentTimeMillis()).setLastUpdateTime(System.currentTimeMillis()).build();

		// WHEN: add an order
		orders.add(order, instrumentModel);

		// THEN: size is ok
		assertThat(orders).hasSize(1);

		// AND: contains is ok
		assertThat(orders.contains(order)).isTrue();
	}
}
