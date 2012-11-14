package com.kensai.market;

import static com.google.common.collect.Lists.newArrayList;
import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;

import org.fest.assertions.api.Assertions;
import org.junit.Test;

import com.kensai.market.core.DepthRow;
import com.kensai.market.core.InsertionResult;
import com.kensai.protocol.Trading.BuySell;
import com.kensai.protocol.Trading.Depth;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.OrderStatus;

public class TestDepthRow {

	@Test
	public void shouldToDepthBuildDepthObject() {
		BuySell side = BuySell.BUY;

		// GIVEN: An empty order
		int firstOrderInitialQty = 7;
		int firstOrderExecutedQty = 0;
		Order firstOrder = OrderBuilderHelper.create(firstOrderInitialQty, firstOrderExecutedQty, side).build();

		// AND: A partially executed order
		int secondOrderInitialQty = 17;
		int secondOrderExecutedQty = 13;
		Order secondOrder = OrderBuilderHelper.create(secondOrderInitialQty, secondOrderExecutedQty, side).build();

		// AND: a DepthRow is created
		double price = 123.456;
		DepthRow row = new DepthRow(price, side, firstOrder, secondOrder);

		// WHEN: build a depth object
		Depth.Builder depth = row.toDepthBuilder();

		// THEN: price and quantity are equals
		assertThat(depth.getPrice()).isEqualTo(price);
		assertThat(depth.getQuantity()).isEqualTo(firstOrderInitialQty - firstOrderExecutedQty + secondOrderInitialQty - secondOrderExecutedQty);
	}

	@Test
	public void shouldCanMakeExecutionsReturnFalseWhenOrdersHasSameSide() {
		BuySell side = BuySell.BUY;

		// GIVEN: An empty order
		int firstOrderInitialQty = 7;
		int firstOrderExecutedQty = 0;
		Order firstOrder = OrderBuilderHelper.create(firstOrderInitialQty, firstOrderExecutedQty, side).build();

		// AND: a DepthRow is created with this order
		double price = 123.456;
		DepthRow row = new DepthRow(price, side, firstOrder);

		// AND: A partially executed order
		int secondOrderInitialQty = 17;
		int secondOrderExecutedQty = 13;
		Order secondOrder = OrderBuilderHelper.create(secondOrderInitialQty, secondOrderExecutedQty, side).build();

		// WHEN: check if insertion of this order could make execution
		boolean couldMakeExecutions = row.couldMakeExecutions(secondOrder);

		// THEN: could not make executions
		assertThat(couldMakeExecutions).isFalse();
	}

	@Test
	public void shouldCanMakeExecutionsReturnTrueWhenDepthIsBuyAndOrderIsSellAndOrderPriceIsLesser() {
		// GIVEN: A sell order
		double orderPrice = 123.456;
		Order order = OrderBuilderHelper.create(orderPrice, 7, 0, BuySell.SELL).build();

		// AND: a DepthRow is created
		double price = 456.789;
		DepthRow row = new DepthRow(price, BuySell.BUY);

		// WHEN: check if insertion of this order could make execution
		boolean couldMakeExecutions = row.couldMakeExecutions(order);

		// THEN: return is true
		assertThat(couldMakeExecutions).isTrue();
	}

	@Test
	public void shouldCanMakeExecutionsReturnTrueWhenDepthIsBuyAndOrderIsSellAndPricesAreEquals() {
		// GIVEN: A sell order
		double orderPrice = 123.456;
		Order order = OrderBuilderHelper.create(orderPrice, 7, 0, BuySell.SELL).build();

		// AND: a DepthRow is created
		DepthRow row = new DepthRow(orderPrice, BuySell.BUY);

		// WHEN: check if insertion of this order could make execution
		boolean couldMakeExecutions = row.couldMakeExecutions(order);

		// THEN: return is true
		assertThat(couldMakeExecutions).isTrue();
	}

	@Test
	public void shouldCanMakeExecutionsReturnTrueWhenDepthIsSellAndOrderIsBuyAndOrderPriceIsGreatter() {
		// GIVEN: A sell order
		double orderPrice = 456.123;
		Order order = OrderBuilderHelper.create(orderPrice, 7, 0, BuySell.BUY).build();

		// AND: a DepthRow is created
		double price = 123.456;
		DepthRow row = new DepthRow(price, BuySell.SELL);

		// WHEN: check if insertion of this order could make execution
		boolean couldMakeExecutions = row.couldMakeExecutions(order);

		// THEN: return is true
		assertThat(couldMakeExecutions).isTrue();
	}

	@Test
	public void shouldCanMakeExecutionsReturnTrueWhenDepthIsSellAndOrderIsBuyAndPricesAreEquals() {
		// GIVEN: A sell order
		double orderPrice = 456.123;
		Order order = OrderBuilderHelper.create(orderPrice, 7, 0, BuySell.BUY).build();

		// AND: a DepthRow is created
		DepthRow row = new DepthRow(orderPrice, BuySell.SELL);

		// WHEN: check if insertion of this order could make execution
		boolean couldMakeExecutions = row.couldMakeExecutions(order);

		// THEN: return is true
		assertThat(couldMakeExecutions).isTrue();
	}

	@Test
	public void shouldInsertThrowsExceptionWhenOrderPriceIsNotSameThanDepthPrice() {
		// GIVEN: A sell order
		double orderPrice = 456.123;
		Order order = OrderBuilderHelper.create(orderPrice, 7, 0, BuySell.BUY).build();

		// AND: a DepthRow is created with different price
		double price = 123.456;
		DepthRow row = new DepthRow(price, BuySell.SELL);

		try {
			// WHEN: insert this order
			row.insert(order);
			Assertions.failBecauseExceptionWasNotThrown(IllegalArgumentException.class);

		} catch (IllegalArgumentException e) {
			// OK
		}
	}

	@Test
	public void shouldInsertInOrderListIfOrderHasSameSide() {
		// GIVEN: A sell order
		BuySell side = BuySell.BUY;
		double orderPrice = 456.123;
		Order order = OrderBuilderHelper.create(orderPrice, 7, 0, side).build();

		// AND: a DepthRow is created
		DepthRow row = new DepthRow(orderPrice, side);

		// WHEN: insert this order
		InsertionResult result = row.insert(order);

		Order resultedOrder = result.getResultedOrder();

		// THEN: result contains only resultedOrder
		assertThat(result.hasExecutedOrders()).isFalse();
		assertThat(result.hasExecutions()).isFalse();
		assertThat(resultedOrder).isNotNull();

		// AND: resulted order is OnMarket
		assertThat(resultedOrder.getOrderStatus()).isEqualTo(OrderStatus.ON_MARKET);

		// AND: DepthRow has been updated
		assertThat(row.size()).isEqualTo(1);
		assertThat(row.getOrderAt(0)).isEqualTo(resultedOrder);
	}

	@Test
	public void shouldInsertSmallOrderInBigDepthMakeExecutions() {
		// GIVEN: An initial big buy order
		BuySell side = BuySell.BUY;
		double price = 456.123;
		int initialQty = 789;
		int initialExecQty = 123;
		Order order = OrderBuilderHelper.create(price, initialQty, initialExecQty, side).build();

		// AND: a DepthRow is created
		DepthRow row = new DepthRow(price, side, order);

		// AND: a small buy order to insert
		int insertQty = 5;
		Order orderToInsert = OrderBuilderHelper.create(price, insertQty, 0, BuySell.SELL).build();

		// WHEN: insert this order
		InsertionResult result = row.insert(orderToInsert);

		Order resultedOrder = result.getResultedOrder();

		// THEN: result contains resultedOrder, two executions and one executed orders
		assertThat(result.hasExecutedOrders()).isTrue();
		assertThat(result.getExecutedOrders()).hasSize(1);
		assertThat(result.hasExecutions()).isTrue();
		assertThat(result.getExecutions()).hasSize(2);
		assertThat(resultedOrder).isNotNull();

		// AND: resulted order is Terminated and fully executed
		assertThat(resultedOrder.getOrderStatus()).isEqualTo(OrderStatus.TERMINATED);
		assertThat(resultedOrder.getExecutedQuantity()).isEqualTo(insertQty);

		// AND: DepthRow has been updated
		assertThat(row.size()).isEqualTo(1);
		assertThat(row.toDepthBuilder().getQuantity()).isEqualTo(initialQty - initialExecQty - insertQty);

		// AND: first order in DepthRow has been updated
		Order firstOrderInDepth = row.getOrderAt(0);
		assertThat(firstOrderInDepth.getExecutedQuantity()).isEqualTo(initialExecQty + insertQty);
	}

	@Test
	public void shouldInsertBigOrderInSmallDepthMakeExecutions() {
		// GIVEN: An initial big buy order
		BuySell side = BuySell.BUY;
		double price = 456.123;
		int initialQty = 123;
		int initialExecQty = 1;
		Order order = OrderBuilderHelper.create(price, initialQty, initialExecQty, side).build();

		// AND: a DepthRow is created
		DepthRow row = new DepthRow(price, side, order);

		// AND: a small buy order to insert
		int insertQty = 456;
		Order orderToInsert = OrderBuilderHelper.create(price, insertQty, 0, BuySell.SELL).build();

		// WHEN: insert this order
		InsertionResult result = row.insert(orderToInsert);

		Order resultedOrder = result.getResultedOrder();

		// THEN: result contains resultedOrder, two executions and one executed orders
		assertThat(result.hasExecutedOrders()).isTrue();
		assertThat(result.getExecutedOrders()).hasSize(1);
		assertThat(result.hasExecutions()).isTrue();
		assertThat(result.getExecutions()).hasSize(2);
		assertThat(resultedOrder).isNotNull();

		// AND: resulted order is OnMarket and not fully executed
		assertThat(resultedOrder.getOrderStatus()).isEqualTo(OrderStatus.ON_MARKET);
		assertThat(resultedOrder.getExecutedQuantity()).isEqualTo(initialQty - initialExecQty);

		// AND: DepthRow is now empty
		assertThat(row.isEmpty());
	}

	@Test
	public void shouldInsertBigOrderInDepthWithManySmallOrdersMakeExecutions() {
		// GIVEN: 5 small orders in BUY
		BuySell side = BuySell.BUY;
		double price = 456.123;
		List<Order> initialOrders = newArrayList();
		initialOrders.add(OrderBuilderHelper.create(price, 2, 1, side).build());
		initialOrders.add(OrderBuilderHelper.create(price, 2, 0, side).build());
		initialOrders.add(OrderBuilderHelper.create(price, 2, 0, side).build());
		initialOrders.add(OrderBuilderHelper.create(price, 2, 0, side).build());
		initialOrders.add(OrderBuilderHelper.create(price, 2, 0, side).build());

		// AND: a DepthRow is created with initial orders
		DepthRow row = new DepthRow(price, side, initialOrders);

		// AND: a small buy order to insert
		int insertQty = 456;
		Order orderToInsert = OrderBuilderHelper.create(price, insertQty, 0, BuySell.SELL).build();

		// WHEN: insert this order
		InsertionResult result = row.insert(orderToInsert);

		Order resultedOrder = result.getResultedOrder();

		// THEN: result contains resultedOrder, two executions and five executed orders
		assertThat(result.hasExecutedOrders()).isTrue();
		assertThat(result.getExecutedOrders()).hasSize(5);
		assertThat(result.hasExecutions()).isTrue();
		assertThat(result.getExecutions()).hasSize(10); // 5 for orderToInsert and 1 for each initial order
		assertThat(resultedOrder).isNotNull();

		// AND: resulted order is OnMarket and not fully executed
		assertThat(resultedOrder.getOrderStatus()).isEqualTo(OrderStatus.ON_MARKET);
		assertThat(resultedOrder.getExecutedQuantity()).isEqualTo(9);

		// AND: DepthRow is now empty
		assertThat(row.isEmpty());
	}
}
