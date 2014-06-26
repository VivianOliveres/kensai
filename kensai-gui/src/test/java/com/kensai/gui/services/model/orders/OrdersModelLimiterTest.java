package com.kensai.gui.services.model.orders;

import static com.kensai.gui.services.connectors.MarketConnector.DEFAULT_USER;
import static com.kensai.protocol.Trading.InstrumentType.STOCK;
import static com.kensai.protocol.Trading.OrderStatus.DELETED;
import static com.kensai.protocol.Trading.OrderStatus.TERMINATED;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.kensai.gui.services.model.instruments.InstrumentModel;
import com.kensai.protocol.Trading.BuySell;
import com.kensai.protocol.Trading.Order;

public class OrdersModelLimiterTest {

	private static final int SIZE = 1;

	private static final InstrumentModel INSTRUMENT = new InstrumentModel("isin", "name", "market", "description", STOCK.toString(), "marketConnectionName");
	private static final OrderModel MODEL = new OrderModel(INSTRUMENT, 123, BuySell.BUY, 123.456, 789);

	private OrdersModelLimiter limiter;

	@Before
	public void init() {
		limiter = new OrdersModelLimiter(SIZE);
	}

	@Test
	public void should_add_simple_OrderModel() {
		// GIVEN: OrderModel

		// WHEN: add
		limiter.add(MODEL);

		// THEN: OrderModel is added
		assertThat(limiter.getOrders()).containsExactly(MODEL);
		assertThat(limiter.size()).isEqualTo(1);

		// AND: contains method should works
		assertThat(limiter.contains(MODEL)).isTrue();
	}

	@Test
	public void should_add_simple_Order() {
		// GIVEN: Order
		Order order = MODEL.toOrder().setUser(DEFAULT_USER).build();

		// WHEN: add
		limiter.add(order, INSTRUMENT);

		// THEN: Order is added
		assertThat(limiter.getOrders()).containsExactly(MODEL);
		assertThat(limiter.size()).isEqualTo(1);

		// AND: contains method should works
		assertThat(limiter.contains(order)).isTrue();
	}

	@Test
	public void should_clean_terminated_orders_in_preference() {
		// GIVEN: One order is added
		Order order1 = MODEL.toOrder().setUser(DEFAULT_USER).build();
		limiter.add(order1, INSTRUMENT);

		// WHEN: Add another Terminated order
		Order order2 = MODEL.toOrder().setUser(DEFAULT_USER).setId(951).setOrderStatus(TERMINATED).build();
		limiter.add(order2, INSTRUMENT);

		// THEN: contains only order1
		assertThat(limiter.getOrders()).containsExactly(MODEL);
		assertThat(limiter.size()).isEqualTo(1);

		// AND: contains method should works
		assertThat(limiter.contains(order1)).isTrue();
		assertThat(limiter.contains(order2)).isFalse();
	}

	@Test
	public void should_clean_deleted_orders_in_preference() {
		// GIVEN: One order is added
		Order order1 = MODEL.toOrder().setUser(DEFAULT_USER).build();
		limiter.add(order1, INSTRUMENT);

		// WHEN: Add another Terminated order
		Order order2 = MODEL.toOrder().setUser(DEFAULT_USER).setId(951).setOrderStatus(DELETED).build();
		limiter.add(order2, INSTRUMENT);

		// THEN: contains only order1
		assertThat(limiter.getOrders()).containsExactly(MODEL);
		assertThat(limiter.size()).isEqualTo(1);

		// AND: contains method should works
		assertThat(limiter.contains(order1)).isTrue();
		assertThat(limiter.contains(order2)).isFalse();
	}

	@Test
	public void should_clean_firstly_added_orders_in_preference() {
		// GIVEN: One order is added
		Order order1 = MODEL.toOrder().setUser(DEFAULT_USER).build();
		limiter.add(order1, INSTRUMENT);

		// WHEN: Add another OnMarket order
		Order order2 = MODEL.toOrder().setUser(DEFAULT_USER).setId(951).build();
		OrderModel model2 = new OrderModel(order2, INSTRUMENT);
		limiter.add(order2, INSTRUMENT);

		// THEN: contains only order1
		assertThat(limiter.getOrders()).containsExactly(model2);
		assertThat(limiter.size()).isEqualTo(1);

		// AND: contains method should works
		assertThat(limiter.contains(order2)).isTrue();
		assertThat(limiter.contains(order1)).isFalse();
	}

	@Test
	public void should_clean_firstly_added_orders_and_deleted_orders_and_terminated_in_preference() {
		// GIVEN: Limiter with bigger size
		limiter = new OrdersModelLimiter(3);

		// AND: One order is added
		Order orderOnMarket1 = MODEL.toOrder().setUser(DEFAULT_USER).build();
		OrderModel orderOnMarketModel1 = new OrderModel(orderOnMarket1, INSTRUMENT);
		limiter.add(orderOnMarket1, INSTRUMENT);

		// AND: Terminated order is added
		Order orderTerminated = MODEL.toOrder().setUser(DEFAULT_USER).setOrderStatus(TERMINATED).setId(843).build();
		limiter.add(orderTerminated, INSTRUMENT);

		// AND: Deleted order is added
		Order orderDeleted = MODEL.toOrder().setUser(DEFAULT_USER).setOrderStatus(DELETED).setId(762).build();
		OrderModel orderDeletedModel = new OrderModel(orderDeleted, INSTRUMENT);
		limiter.add(orderDeleted, INSTRUMENT);

		// WHEN: add another OnMarket order
		Order orderOnMarket2 = MODEL.toOrder().setUser(DEFAULT_USER).setId(159).build();
		OrderModel orderOnMarketModel2 = new OrderModel(orderOnMarket2, INSTRUMENT);
		limiter.add(orderOnMarket2, INSTRUMENT);

		// THEN: contains only orderOnMarket1, orderOnMarket2 and orderDeleted
		assertThat(limiter.getOrders()).containsOnly(orderOnMarketModel1, orderOnMarketModel2, orderDeletedModel);
		assertThat(limiter.size()).isEqualTo(3);

		// AND: contains method should works
		assertThat(limiter.contains(orderOnMarket1)).isTrue();
		assertThat(limiter.contains(orderTerminated)).isFalse();
		assertThat(limiter.contains(orderOnMarket2)).isTrue();
		assertThat(limiter.contains(orderDeleted)).isTrue();

		// WHEN: add another OnMarket order
		Order orderOnMarket3 = MODEL.toOrder().setUser(DEFAULT_USER).setId(483).build();
		OrderModel orderOnMarketModel3 = new OrderModel(orderOnMarket3, INSTRUMENT);
		limiter.add(orderOnMarket3, INSTRUMENT);

		// THEN: contains only orderOnMarket1, orderOnMarket2 and orderOnMarket3
		assertThat(limiter.getOrders()).containsOnly(orderOnMarketModel1, orderOnMarketModel2, orderOnMarketModel3);
		assertThat(limiter.size()).isEqualTo(3);

		// AND: contains method should works
		assertThat(limiter.contains(orderOnMarket1)).isTrue();
		assertThat(limiter.contains(orderTerminated)).isFalse();
		assertThat(limiter.contains(orderOnMarket2)).isTrue();
		assertThat(limiter.contains(orderDeleted)).isFalse();
		assertThat(limiter.contains(orderOnMarket3)).isTrue();

		// WHEN: add another OnMarket order
		Order orderOnMarket4 = MODEL.toOrder().setUser(DEFAULT_USER).setId(726).build();
		OrderModel orderOnMarketModel4 = new OrderModel(orderOnMarket4, INSTRUMENT);
		limiter.add(orderOnMarket4, INSTRUMENT);

		// THEN: contains only orderOnMarket2 and orderOnMarket3, orderOnMarket4
		assertThat(limiter.getOrders()).containsOnly(orderOnMarketModel2, orderOnMarketModel3, orderOnMarketModel4);
		assertThat(limiter.size()).isEqualTo(3);

		// AND: contains method should works
		assertThat(limiter.contains(orderOnMarket1)).isFalse();
		assertThat(limiter.contains(orderTerminated)).isFalse();
		assertThat(limiter.contains(orderOnMarket2)).isTrue();
		assertThat(limiter.contains(orderDeleted)).isFalse();
		assertThat(limiter.contains(orderOnMarket3)).isTrue();
		assertThat(limiter.contains(orderOnMarket4)).isTrue();
	}
}
