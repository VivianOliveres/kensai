package com.kensai.market.core;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.kensai.market.factories.OrderFactory;
import com.kensai.protocol.Trading.BuySell;
import com.kensai.protocol.Trading.CommandStatus;
import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.OrderStatus;
import com.kensai.protocol.Trading.Summary;

public class TestInstrumentDepth {

	private Instrument instr = OrderFactory.INSTRUMENT;
	private double open = 15;
	private double close = 20;

	private String user = OrderFactory.USER;

	private InstrumentDepth depth;

	@Before
	public void init() {
		depth = new InstrumentDepth(instr, open, close);
	}

	@Test
	public void shouldInsertBuyOrderInEmptyInstrumentDepth() {
		// GIVEN: a buy order to insert
		double price = 123.456;
		int initialQty = 99;
		BuySell side = BuySell.BUY;
		Order order = OrderFactory.create(price, initialQty, side).build();

		// WHEN: Insert this order
		InsertionResult result = depth.insert(order);

		// THEN: result contains only resultedOrder
		Order resultedOrder = result.getResultedOrder();
		assertThat(resultedOrder).isNotNull();
		assertThat(result.hasExecutedOrders()).isFalse();
		assertThat(result.hasExecutions()).isFalse();

		// AND: resulted order is "same" than one inserted
		assertThat(resultedOrder.getInstrument()).isEqualTo(instr);
		assertThat(resultedOrder.getInitialQuantity()).isEqualTo(initialQty);
		assertThat(resultedOrder.getSide()).isEqualTo(side);
		assertThat(resultedOrder.getPrice()).isEqualTo(price);
		assertThat(resultedOrder.getUserData()).isEqualTo(order.getUserData());

		// AND: resulted order has been correctly updated
		assertThat(resultedOrder.getOrderStatus()).isEqualTo(OrderStatus.ON_MARKET);
		assertThat(resultedOrder.getCommandStatus()).isEqualTo(CommandStatus.ACK);
		assertThat(resultedOrder.getId()).isNotNull().isGreaterThan(1);

		// AND: resulted order is into depth and there is no other order
		assertThat(depth.getAllExecutions()).isEmpty();
		assertThat(depth.getAllOrders()).containsOnly(resultedOrder);
		assertThat(depth.getAllOrders(user)).containsOnly(resultedOrder);
		assertThat(depth.getAllActiveOrders()).containsOnly(resultedOrder);
		assertThat(depth.getAllActiveOrders(user)).containsOnly(resultedOrder);
	}

	@Test
	public void shouldInsertSellOrderInEmptyInstrumentDepth() {
		// GIVEN: a buy order to insert
		double price = 123.456;
		int initialQty = 99;
		BuySell side = BuySell.SELL;
		Order order = OrderFactory.create(price, initialQty, side).build();

		// WHEN: Insert this order
		InsertionResult result = depth.insert(order);

		// THEN: result contains only resultedOrder
		Order resultedOrder = result.getResultedOrder();
		assertThat(resultedOrder).isNotNull();
		assertThat(result.hasExecutedOrders()).isFalse();
		assertThat(result.hasExecutions()).isFalse();

		// AND: resulted order is "same" than one inserted
		assertThat(resultedOrder.getInstrument()).isEqualTo(instr);
		assertThat(resultedOrder.getInitialQuantity()).isEqualTo(initialQty);
		assertThat(resultedOrder.getSide()).isEqualTo(side);
		assertThat(resultedOrder.getPrice()).isEqualTo(price);
		assertThat(resultedOrder.getUserData()).isEqualTo(order.getUserData());

		// AND: resulted order has been correctly updated
		assertThat(resultedOrder.getOrderStatus()).isEqualTo(OrderStatus.ON_MARKET);
		assertThat(resultedOrder.getCommandStatus()).isEqualTo(CommandStatus.ACK);
		assertThat(resultedOrder.getId()).isNotNull().isGreaterThan(1);

		// AND: resulted order is into depth and there is no other order
		assertThat(depth.getAllExecutions()).isEmpty();
		assertThat(depth.getAllOrders()).containsOnly(resultedOrder);
		assertThat(depth.getAllOrders(user)).containsOnly(resultedOrder);
		assertThat(depth.getAllActiveOrders()).containsOnly(resultedOrder);
		assertThat(depth.getAllActiveOrders(user)).containsOnly(resultedOrder);
	}

	// //////////////////////////////////////////

	@Test
	public void shouldInsertBuyOrderWithEmptyBuyDepthAndNotEmptySellDepthAndNoExec() {
		// GIVEN: a sell order is already in depth
		Order oppositeOrder = OrderFactory.create(456.123, 753, BuySell.SELL).build();
		Order oppositeOrderInserted = depth.insert(oppositeOrder).getResultedOrder();

		// AND: a buy order to insert
		double price = 123.456;
		int initialQty = 99;
		BuySell side = BuySell.BUY;
		Order order = OrderFactory.create(price, initialQty, side).build();

		// WHEN: Insert this order
		InsertionResult result = depth.insert(order);

		// THEN: result contains only resultedOrder
		Order resultedOrder = result.getResultedOrder();
		assertThat(resultedOrder).isNotNull();
		assertThat(result.hasExecutedOrders()).isFalse();
		assertThat(result.hasExecutions()).isFalse();

		// AND: resulted order is "same" than one inserted
		assertThat(resultedOrder.getInstrument()).isEqualTo(instr);
		assertThat(resultedOrder.getInitialQuantity()).isEqualTo(initialQty);
		assertThat(resultedOrder.getSide()).isEqualTo(side);
		assertThat(resultedOrder.getPrice()).isEqualTo(price);
		assertThat(resultedOrder.getUserData()).isEqualTo(order.getUserData());

		// AND: resulted order has been correctly updated
		assertThat(resultedOrder.getOrderStatus()).isEqualTo(OrderStatus.ON_MARKET);
		assertThat(resultedOrder.getCommandStatus()).isEqualTo(CommandStatus.ACK);
		assertThat(resultedOrder.getId()).isNotNull().isGreaterThan(1);

		// AND: resulted order is into depth and there is no other order
		assertThat(depth.getAllExecutions()).isEmpty();
		assertThat(depth.getAllOrders()).containsOnly(resultedOrder, oppositeOrderInserted);
		assertThat(depth.getAllOrders(user)).containsOnly(resultedOrder, oppositeOrderInserted);
		assertThat(depth.getAllActiveOrders()).containsOnly(resultedOrder, oppositeOrderInserted);
		assertThat(depth.getAllActiveOrders(user)).containsOnly(resultedOrder, oppositeOrderInserted);
	}

	@Test
	public void shouldInsertSellOrderWithEmptySellDepthAndNotEmptyBuyDepthAndNoExec() {
		// GIVEN: a sell order is already in depth
		Order oppositeOrder = OrderFactory.create(123.456, 753, BuySell.BUY).build();
		Order oppositeOrderOrderInserted = depth.insert(oppositeOrder).getResultedOrder();

		// AND: a buy order to insert
		double price = 456.123;
		int initialQty = 99;
		BuySell side = BuySell.SELL;
		Order order = OrderFactory.create(price, initialQty, side).build();

		// WHEN: Insert this order
		InsertionResult result = depth.insert(order);

		// THEN: result contains only resultedOrder
		Order resultedOrder = result.getResultedOrder();
		assertThat(resultedOrder).isNotNull();
		assertThat(result.hasExecutedOrders()).isFalse();
		assertThat(result.hasExecutions()).isFalse();

		// AND: resulted order is "same" than one inserted
		assertThat(resultedOrder.getInstrument()).isEqualTo(instr);
		assertThat(resultedOrder.getInitialQuantity()).isEqualTo(initialQty);
		assertThat(resultedOrder.getSide()).isEqualTo(side);
		assertThat(resultedOrder.getPrice()).isEqualTo(price);
		assertThat(resultedOrder.getUserData()).isEqualTo(order.getUserData());

		// AND: resulted order has been correctly updated
		assertThat(resultedOrder.getOrderStatus()).isEqualTo(OrderStatus.ON_MARKET);
		assertThat(resultedOrder.getCommandStatus()).isEqualTo(CommandStatus.ACK);
		assertThat(resultedOrder.getId()).isNotNull().isGreaterThan(1);

		// AND: resulted order is into depth and there is no other order
		assertThat(depth.getAllExecutions()).isEmpty();
		assertThat(depth.getAllOrders()).containsOnly(resultedOrder, oppositeOrderOrderInserted);
		assertThat(depth.getAllOrders(user)).containsOnly(resultedOrder, oppositeOrderOrderInserted);
		assertThat(depth.getAllActiveOrders()).containsOnly(resultedOrder, oppositeOrderOrderInserted);
		assertThat(depth.getAllActiveOrders(user)).containsOnly(resultedOrder, oppositeOrderOrderInserted);
	}

	// //////////////////////////////////////////

	@Test
	public void shouldInsertBuyOrderOnTopWithEmptySellDepthAndNoExec() {
		// GIVEN: a buy order with low price is already in depth
		Order initialOrder = OrderFactory.create(123.456, 753, BuySell.BUY).build();
		Order initialOrderInserted = depth.insert(initialOrder).getResultedOrder();

		// AND: a buy order with high price to insert
		double price = 456.123;
		int initialQty = 99;
		BuySell side = BuySell.BUY;
		Order order = OrderFactory.create(price, initialQty, side).build();

		// WHEN: Insert this order
		InsertionResult result = depth.insert(order);

		// THEN: result contains only resultedOrder
		Order resultedOrder = result.getResultedOrder();
		assertThat(resultedOrder).isNotNull();
		assertThat(result.hasExecutedOrders()).isFalse();
		assertThat(result.hasExecutions()).isFalse();

		// AND: resulted order is "same" than one inserted
		assertThat(resultedOrder.getInstrument()).isEqualTo(instr);
		assertThat(resultedOrder.getInitialQuantity()).isEqualTo(initialQty);
		assertThat(resultedOrder.getSide()).isEqualTo(side);
		assertThat(resultedOrder.getPrice()).isEqualTo(price);
		assertThat(resultedOrder.getUserData()).isEqualTo(order.getUserData());

		// AND: resulted order has been correctly updated
		assertThat(resultedOrder.getOrderStatus()).isEqualTo(OrderStatus.ON_MARKET);
		assertThat(resultedOrder.getCommandStatus()).isEqualTo(CommandStatus.ACK);
		assertThat(resultedOrder.getId()).isNotNull().isGreaterThan(1);

		// AND: resulted order is into depth and there is no other order
		assertThat(depth.getAllExecutions()).isEmpty();
		assertThat(depth.getAllOrders()).containsOnly(resultedOrder, initialOrderInserted);
		assertThat(depth.getAllOrders(user)).containsOnly(resultedOrder, initialOrderInserted);
		assertThat(depth.getAllActiveOrders()).containsOnly(resultedOrder, initialOrderInserted);
		assertThat(depth.getAllActiveOrders(user)).containsOnly(resultedOrder, initialOrderInserted);

		// AND: there is two buy depth
		Summary summary = depth.toSummary();
		assertThat(summary).isNotNull();
		assertThat(summary.getSellDepthsCount()).isZero();
		assertThat(summary.getBuyDepthsCount()).isEqualTo(2);

		// AND: first depth is from resulted order
		assertThat(summary.getBuyDepths(0).getPrice()).isEqualTo(price);
		assertThat(summary.getBuyDepths(0).getQuantity()).isEqualTo(initialQty);

		// AND: second depth is from initial order
		assertThat(summary.getBuyDepths(1).getPrice()).isEqualTo(initialOrderInserted.getPrice());
		assertThat(summary.getBuyDepths(1).getQuantity()).isEqualTo(initialOrderInserted.getInitialQuantity());
	}

	@Test
	public void shouldInsertBuyOrderOnBottomWithEmptySellDepthAndNoExec() {
		// GIVEN: a buy order with high price is already in depth
		Order initialOrder = OrderFactory.create(456.123, 753, BuySell.BUY).build();
		Order initialOrderInserted = depth.insert(initialOrder).getResultedOrder();

		// AND: a buy order with low price to insert
		double price = 123.456;
		int initialQty = 99;
		BuySell side = BuySell.BUY;
		Order order = OrderFactory.create(price, initialQty, side).build();

		// WHEN: Insert this order
		InsertionResult result = depth.insert(order);

		// THEN: result contains only resultedOrder
		Order resultedOrder = result.getResultedOrder();
		assertThat(resultedOrder).isNotNull();
		assertThat(result.hasExecutedOrders()).isFalse();
		assertThat(result.hasExecutions()).isFalse();

		// AND: resulted order is "same" than one inserted
		assertThat(resultedOrder.getInstrument()).isEqualTo(instr);
		assertThat(resultedOrder.getInitialQuantity()).isEqualTo(initialQty);
		assertThat(resultedOrder.getSide()).isEqualTo(side);
		assertThat(resultedOrder.getPrice()).isEqualTo(price);
		assertThat(resultedOrder.getUserData()).isEqualTo(order.getUserData());

		// AND: resulted order has been correctly updated
		assertThat(resultedOrder.getOrderStatus()).isEqualTo(OrderStatus.ON_MARKET);
		assertThat(resultedOrder.getCommandStatus()).isEqualTo(CommandStatus.ACK);
		assertThat(resultedOrder.getId()).isNotNull().isGreaterThan(1);

		// AND: resulted order is into depth and there is no other order
		assertThat(depth.getAllExecutions()).isEmpty();
		assertThat(depth.getAllOrders()).containsOnly(resultedOrder, initialOrderInserted);
		assertThat(depth.getAllOrders(user)).containsOnly(resultedOrder, initialOrderInserted);
		assertThat(depth.getAllActiveOrders()).containsOnly(resultedOrder, initialOrderInserted);
		assertThat(depth.getAllActiveOrders(user)).containsOnly(resultedOrder, initialOrderInserted);

		// AND: there is two buy depth
		Summary summary = depth.toSummary();
		assertThat(summary).isNotNull();
		assertThat(summary.getSellDepthsCount()).isZero();
		assertThat(summary.getBuyDepthsCount()).isEqualTo(2);

		// AND: second depth is from resulted order
		assertThat(summary.getBuyDepths(1).getPrice()).isEqualTo(price);
		assertThat(summary.getBuyDepths(1).getQuantity()).isEqualTo(initialQty);

		// AND: first depth is from initial order
		assertThat(summary.getBuyDepths(0).getPrice()).isEqualTo(initialOrderInserted.getPrice());
		assertThat(summary.getBuyDepths(0).getQuantity()).isEqualTo(initialOrderInserted.getInitialQuantity());
	}

	@Test
	public void shouldInsertBuyOrderOnSameLevelWithEmptySellDepthAndNoExec() {
		// GIVEN: a buy order is already in depth
		double price = 123.456;
		Order initialOrder = OrderFactory.create(price, 753, BuySell.BUY).build();
		Order initialOrderInserted = depth.insert(initialOrder).getResultedOrder();

		// AND: a buy order with same price to insert
		int initialQty = 99;
		BuySell side = BuySell.BUY;
		Order order = OrderFactory.create(price, initialQty, side).build();

		// WHEN: Insert this order
		InsertionResult result = depth.insert(order);

		// THEN: result contains only resultedOrder
		Order resultedOrder = result.getResultedOrder();
		assertThat(resultedOrder).isNotNull();
		assertThat(result.hasExecutedOrders()).isFalse();
		assertThat(result.hasExecutions()).isFalse();

		// AND: resulted order is "same" than one inserted
		assertThat(resultedOrder.getInstrument()).isEqualTo(instr);
		assertThat(resultedOrder.getInitialQuantity()).isEqualTo(initialQty);
		assertThat(resultedOrder.getSide()).isEqualTo(side);
		assertThat(resultedOrder.getPrice()).isEqualTo(price);
		assertThat(resultedOrder.getUserData()).isEqualTo(order.getUserData());

		// AND: resulted order has been correctly updated
		assertThat(resultedOrder.getOrderStatus()).isEqualTo(OrderStatus.ON_MARKET);
		assertThat(resultedOrder.getCommandStatus()).isEqualTo(CommandStatus.ACK);
		assertThat(resultedOrder.getId()).isNotNull().isGreaterThan(1);

		// AND: resulted order is into depth and there is no other order
		assertThat(depth.getAllExecutions()).isEmpty();
		assertThat(depth.getAllOrders()).containsOnly(resultedOrder, initialOrderInserted);
		assertThat(depth.getAllOrders(user)).containsOnly(resultedOrder, initialOrderInserted);
		assertThat(depth.getAllActiveOrders()).containsOnly(resultedOrder, initialOrderInserted);
		assertThat(depth.getAllActiveOrders(user)).containsOnly(resultedOrder, initialOrderInserted);

		// AND: there is only one buy depth
		Summary summary = depth.toSummary();
		assertThat(summary).isNotNull();
		assertThat(summary.getSellDepthsCount()).isZero();
		assertThat(summary.getBuyDepthsCount()).isEqualTo(1);

		// AND: first depth contains initial order and inserted order
		assertThat(summary.getBuyDepths(0).getPrice()).isEqualTo(price);
		assertThat(summary.getBuyDepths(0).getQuantity()).isEqualTo(initialOrderInserted.getInitialQuantity() + initialQty);
	}

	@Test
	public void shouldInsertBuyOrderBetweenTwoLevelWithEmptySellDepthAndNoExec() {
		BuySell side = BuySell.BUY;

		// GIVEN: a buy order with high price is already in depth
		Order topOrder = OrderFactory.create(789.456, 753, side).build();
		Order topOrderInserted = depth.insert(topOrder).getResultedOrder();

		// AND: a buy order with low price is already in depth
		Order bottomOrder = OrderFactory.create(123.456, 159, side).build();
		Order bottomOrderInserted = depth.insert(bottomOrder).getResultedOrder();

		// AND: a buy order with middle price to insert
		int initialQty = 99;
		double price = 456.456;
		Order order = OrderFactory.create(price, initialQty, side).build();

		// WHEN: Insert this order
		InsertionResult result = depth.insert(order);

		// THEN: result contains only resultedOrder
		Order resultedOrder = result.getResultedOrder();
		assertThat(resultedOrder).isNotNull();
		assertThat(result.hasExecutedOrders()).isFalse();
		assertThat(result.hasExecutions()).isFalse();

		// AND: resulted order is "same" than one inserted
		assertThat(resultedOrder.getInstrument()).isEqualTo(instr);
		assertThat(resultedOrder.getInitialQuantity()).isEqualTo(initialQty);
		assertThat(resultedOrder.getSide()).isEqualTo(side);
		assertThat(resultedOrder.getPrice()).isEqualTo(price);
		assertThat(resultedOrder.getUserData()).isEqualTo(order.getUserData());

		// AND: resulted order has been correctly updated
		assertThat(resultedOrder.getOrderStatus()).isEqualTo(OrderStatus.ON_MARKET);
		assertThat(resultedOrder.getCommandStatus()).isEqualTo(CommandStatus.ACK);
		assertThat(resultedOrder.getId()).isNotNull().isGreaterThan(1);

		// AND: resulted order is into depth and there is no other order
		assertThat(depth.getAllExecutions()).isEmpty();
		assertThat(depth.getAllOrders()).containsOnly(topOrderInserted, bottomOrderInserted, resultedOrder);
		assertThat(depth.getAllOrders(user)).containsOnly(topOrderInserted, bottomOrderInserted, resultedOrder);
		assertThat(depth.getAllActiveOrders()).containsOnly(topOrderInserted, bottomOrderInserted, resultedOrder);
		assertThat(depth.getAllActiveOrders(user)).containsOnly(topOrderInserted, bottomOrderInserted, resultedOrder);

		// AND: there is only 3 buy depth
		Summary summary = depth.toSummary();
		assertThat(summary).isNotNull();
		assertThat(summary.getSellDepthsCount()).isZero();
		assertThat(summary.getBuyDepthsCount()).isEqualTo(3);

		// AND: first depth contains top order
		assertThat(summary.getBuyDepths(0).getPrice()).isEqualTo(topOrderInserted.getPrice());
		assertThat(summary.getBuyDepths(0).getQuantity()).isEqualTo(topOrderInserted.getInitialQuantity());

		// AND: second depth contains resulted order
		assertThat(summary.getBuyDepths(1).getPrice()).isEqualTo(resultedOrder.getPrice());
		assertThat(summary.getBuyDepths(1).getQuantity()).isEqualTo(resultedOrder.getInitialQuantity());

		// AND: third depth contains bottom order
		assertThat(summary.getBuyDepths(2).getPrice()).isEqualTo(bottomOrderInserted.getPrice());
		assertThat(summary.getBuyDepths(2).getQuantity()).isEqualTo(bottomOrderInserted.getInitialQuantity());
	}

	@Test
	public void shouldInsertSellOrderOnTopWithEmptyBuyDepthAndNoExec() {
		// GIVEN: a sell order with high price is already in depth
		Order initialOrder = OrderFactory.create(456.123, 753, BuySell.SELL).build();
		Order initialOrderInserted = depth.insert(initialOrder).getResultedOrder();

		// AND: a sell order with low price to insert
		double price = 123.456;
		int initialQty = 99;
		BuySell side = BuySell.SELL;
		Order order = OrderFactory.create(price, initialQty, side).build();

		// WHEN: Insert this order
		InsertionResult result = depth.insert(order);

		// THEN: result contains only resultedOrder
		Order resultedOrder = result.getResultedOrder();
		assertThat(resultedOrder).isNotNull();
		assertThat(result.hasExecutedOrders()).isFalse();
		assertThat(result.hasExecutions()).isFalse();

		// AND: resulted order is "same" than one inserted
		assertThat(resultedOrder.getInstrument()).isEqualTo(instr);
		assertThat(resultedOrder.getInitialQuantity()).isEqualTo(initialQty);
		assertThat(resultedOrder.getSide()).isEqualTo(side);
		assertThat(resultedOrder.getPrice()).isEqualTo(price);
		assertThat(resultedOrder.getUserData()).isEqualTo(order.getUserData());

		// AND: resulted order has been correctly updated
		assertThat(resultedOrder.getOrderStatus()).isEqualTo(OrderStatus.ON_MARKET);
		assertThat(resultedOrder.getCommandStatus()).isEqualTo(CommandStatus.ACK);
		assertThat(resultedOrder.getId()).isNotNull().isGreaterThan(1);

		// AND: resulted order is into depth and there is no other order
		assertThat(depth.getAllExecutions()).isEmpty();
		assertThat(depth.getAllOrders()).containsOnly(resultedOrder, initialOrderInserted);
		assertThat(depth.getAllOrders(user)).containsOnly(resultedOrder, initialOrderInserted);
		assertThat(depth.getAllActiveOrders()).containsOnly(resultedOrder, initialOrderInserted);
		assertThat(depth.getAllActiveOrders(user)).containsOnly(resultedOrder, initialOrderInserted);

		// AND: there is two buy depth
		Summary summary = depth.toSummary();
		assertThat(summary).isNotNull();
		assertThat(summary.getSellDepthsCount()).isEqualTo(2);
		assertThat(summary.getBuyDepthsCount()).isZero();

		// AND: first depth is from resulted order
		assertThat(summary.getSellDepths(0).getPrice()).isEqualTo(price);
		assertThat(summary.getSellDepths(0).getQuantity()).isEqualTo(initialQty);

		// AND: second depth is from initial order
		assertThat(summary.getSellDepths(1).getPrice()).isEqualTo(initialOrderInserted.getPrice());
		assertThat(summary.getSellDepths(1).getQuantity()).isEqualTo(initialOrderInserted.getInitialQuantity());
	}

	@Test
	public void shouldInsertSellOrderOnBottomWithEmptyBuyDepthAndNoExec() {
		// GIVEN: a sell order with low price is already in depth
		Order initialOrder = OrderFactory.create(123.456, 753, BuySell.SELL).build();
		Order initialOrderInserted = depth.insert(initialOrder).getResultedOrder();

		// AND: a sell order with high price to insert
		double price = 456.123;
		int initialQty = 99;
		BuySell side = BuySell.SELL;
		Order order = OrderFactory.create(price, initialQty, side).build();

		// WHEN: Insert this order
		InsertionResult result = depth.insert(order);

		// THEN: result contains only resultedOrder
		Order resultedOrder = result.getResultedOrder();
		assertThat(resultedOrder).isNotNull();
		assertThat(result.hasExecutedOrders()).isFalse();
		assertThat(result.hasExecutions()).isFalse();

		// AND: resulted order is "same" than one inserted
		assertThat(resultedOrder.getInstrument()).isEqualTo(instr);
		assertThat(resultedOrder.getInitialQuantity()).isEqualTo(initialQty);
		assertThat(resultedOrder.getSide()).isEqualTo(side);
		assertThat(resultedOrder.getPrice()).isEqualTo(price);
		assertThat(resultedOrder.getUserData()).isEqualTo(order.getUserData());

		// AND: resulted order has been correctly updated
		assertThat(resultedOrder.getOrderStatus()).isEqualTo(OrderStatus.ON_MARKET);
		assertThat(resultedOrder.getCommandStatus()).isEqualTo(CommandStatus.ACK);
		assertThat(resultedOrder.getId()).isNotNull().isGreaterThan(1);

		// AND: resulted order is into depth and there is no other order
		assertThat(depth.getAllExecutions()).isEmpty();
		assertThat(depth.getAllOrders()).containsOnly(resultedOrder, initialOrderInserted);
		assertThat(depth.getAllOrders(user)).containsOnly(resultedOrder, initialOrderInserted);
		assertThat(depth.getAllActiveOrders()).containsOnly(resultedOrder, initialOrderInserted);
		assertThat(depth.getAllActiveOrders(user)).containsOnly(resultedOrder, initialOrderInserted);

		// AND: there is two buy depth
		Summary summary = depth.toSummary();
		assertThat(summary).isNotNull();
		assertThat(summary.getSellDepthsCount()).isEqualTo(2);
		assertThat(summary.getBuyDepthsCount()).isZero();

		// AND: second depth is from resulted order
		assertThat(summary.getSellDepths(1).getPrice()).isEqualTo(price);
		assertThat(summary.getSellDepths(1).getQuantity()).isEqualTo(initialQty);

		// AND: first depth is from initial order
		assertThat(summary.getSellDepths(0).getPrice()).isEqualTo(initialOrderInserted.getPrice());
		assertThat(summary.getSellDepths(0).getQuantity()).isEqualTo(initialOrderInserted.getInitialQuantity());
	}

	@Test
	public void shouldInsertSellOrderOnSameLevelWithEmptySellDepthAndNoExec() {
		// GIVEN: a sell order is already in depth
		double price = 123.456;
		BuySell side = BuySell.SELL;
		Order initialOrder = OrderFactory.create(price, 753, side).build();
		Order initialOrderInserted = depth.insert(initialOrder).getResultedOrder();

		// AND: a sell order with same price to insert
		int initialQty = 99;
		Order order = OrderFactory.create(price, initialQty, side).build();

		// WHEN: Insert this order
		InsertionResult result = depth.insert(order);

		// THEN: result contains only resultedOrder
		Order resultedOrder = result.getResultedOrder();
		assertThat(resultedOrder).isNotNull();
		assertThat(result.hasExecutedOrders()).isFalse();
		assertThat(result.hasExecutions()).isFalse();

		// AND: resulted order is "same" than one inserted
		assertThat(resultedOrder.getInstrument()).isEqualTo(instr);
		assertThat(resultedOrder.getInitialQuantity()).isEqualTo(initialQty);
		assertThat(resultedOrder.getSide()).isEqualTo(side);
		assertThat(resultedOrder.getPrice()).isEqualTo(price);
		assertThat(resultedOrder.getUserData()).isEqualTo(order.getUserData());

		// AND: resulted order has been correctly updated
		assertThat(resultedOrder.getOrderStatus()).isEqualTo(OrderStatus.ON_MARKET);
		assertThat(resultedOrder.getCommandStatus()).isEqualTo(CommandStatus.ACK);
		assertThat(resultedOrder.getId()).isNotNull().isGreaterThan(1);

		// AND: resulted order is into depth and there is no other order
		assertThat(depth.getAllExecutions()).isEmpty();
		assertThat(depth.getAllOrders()).containsOnly(resultedOrder, initialOrderInserted);
		assertThat(depth.getAllOrders(user)).containsOnly(resultedOrder, initialOrderInserted);
		assertThat(depth.getAllActiveOrders()).containsOnly(resultedOrder, initialOrderInserted);
		assertThat(depth.getAllActiveOrders(user)).containsOnly(resultedOrder, initialOrderInserted);

		// AND: there is only one buy depth
		Summary summary = depth.toSummary();
		assertThat(summary).isNotNull();
		assertThat(summary.getSellDepthsCount()).isEqualTo(1);
		assertThat(summary.getBuyDepthsCount()).isZero();

		// AND: first depth contains initial order and inserted order
		assertThat(summary.getSellDepths(0).getPrice()).isEqualTo(price);
		assertThat(summary.getSellDepths(0).getQuantity()).isEqualTo(initialOrderInserted.getInitialQuantity() + initialQty);
	}

	@Test
	public void shouldInsertSellOrderBetweenTwoLevelWithEmptyBuyDepthAndNoExec() {
		BuySell side = BuySell.SELL;

		// GIVEN: a sell order with low price is already in depth
		Order topOrder = OrderFactory.create(123.123, 753, side).build();
		Order topOrderInserted = depth.insert(topOrder).getResultedOrder();

		// AND: a sell order with high price is already in depth
		Order bottomOrder = OrderFactory.create(789.789, 159, side).build();
		Order bottomOrderInserted = depth.insert(bottomOrder).getResultedOrder();

		// AND: a sell order with middle price to insert
		int initialQty = 99;
		double price = 456.456;
		Order order = OrderFactory.create(price, initialQty, side).build();

		// WHEN: Insert this order
		InsertionResult result = depth.insert(order);

		// THEN: result contains only resultedOrder
		Order resultedOrder = result.getResultedOrder();
		assertThat(resultedOrder).isNotNull();
		assertThat(result.hasExecutedOrders()).isFalse();
		assertThat(result.hasExecutions()).isFalse();

		// AND: resulted order is "same" than one inserted
		assertThat(resultedOrder.getInstrument()).isEqualTo(instr);
		assertThat(resultedOrder.getInitialQuantity()).isEqualTo(initialQty);
		assertThat(resultedOrder.getSide()).isEqualTo(side);
		assertThat(resultedOrder.getPrice()).isEqualTo(price);
		assertThat(resultedOrder.getUserData()).isEqualTo(order.getUserData());

		// AND: resulted order has been correctly updated
		assertThat(resultedOrder.getOrderStatus()).isEqualTo(OrderStatus.ON_MARKET);
		assertThat(resultedOrder.getCommandStatus()).isEqualTo(CommandStatus.ACK);
		assertThat(resultedOrder.getId()).isNotNull().isGreaterThan(1);

		// AND: resulted order is into depth and there is no other order
		assertThat(depth.getAllExecutions()).isEmpty();
		assertThat(depth.getAllOrders()).containsOnly(topOrderInserted, bottomOrderInserted, resultedOrder);
		assertThat(depth.getAllOrders(user)).containsOnly(topOrderInserted, bottomOrderInserted, resultedOrder);
		assertThat(depth.getAllActiveOrders()).containsOnly(topOrderInserted, bottomOrderInserted, resultedOrder);
		assertThat(depth.getAllActiveOrders(user)).containsOnly(topOrderInserted, bottomOrderInserted, resultedOrder);

		// AND: there is only 3 sell depth
		Summary summary = depth.toSummary();
		assertThat(summary).isNotNull();
		assertThat(summary.getBuyDepthsCount()).isZero();
		assertThat(summary.getSellDepthsCount()).isEqualTo(3);

		// AND: first depth contains top order
		assertThat(summary.getSellDepths(0).getPrice()).isEqualTo(topOrderInserted.getPrice());
		assertThat(summary.getSellDepths(0).getQuantity()).isEqualTo(topOrderInserted.getInitialQuantity());

		// AND: second depth contains resulted order
		assertThat(summary.getSellDepths(1).getPrice()).isEqualTo(resultedOrder.getPrice());
		assertThat(summary.getSellDepths(1).getQuantity()).isEqualTo(resultedOrder.getInitialQuantity());

		// AND: third depth contains bottom order
		assertThat(summary.getSellDepths(2).getPrice()).isEqualTo(bottomOrderInserted.getPrice());
		assertThat(summary.getSellDepths(2).getQuantity()).isEqualTo(bottomOrderInserted.getInitialQuantity());
	}

	// ////////////////////////////////////////////

	@Test
	public void shouldInsertSmallBuyOrderWithExec() {
		// GIVEN: depth contains big sell order
		int initialQty = 999;
		double price = 123.123;
		Order initialOrder = OrderFactory.create(price, initialQty, BuySell.SELL).build();
		Order initialOrderInserted = depth.insert(initialOrder).getResultedOrder();

		// AND: a buy order with same price and small quantity has to be inserted
		int qty = 5;
		BuySell side = BuySell.BUY;
		Order order = OrderFactory.create(price, qty, side).build();

		// WHEN: insert order
		InsertionResult result = depth.insert(order);

		// THEN: result contains resultedOrder, 2 exec, and 1 executed order
		Order resultedOrder = result.getResultedOrder();
		assertThat(resultedOrder).isNotNull();
		assertThat(result.hasExecutedOrders()).isTrue();
		assertThat(result.getExecutedOrders()).hasSize(1);
		assertThat(result.hasExecutions()).isTrue();
		assertThat(result.getExecutions()).hasSize(2);

		// AND: resulted order is "same" than one inserted
		assertThat(resultedOrder.getInstrument()).isEqualTo(instr);
		assertThat(resultedOrder.getInitialQuantity()).isEqualTo(qty);
		assertThat(resultedOrder.getSide()).isEqualTo(side);
		assertThat(resultedOrder.getPrice()).isEqualTo(price);
		assertThat(resultedOrder.getUserData()).isEqualTo(order.getUserData());

		// AND: resulted order has been correctly updated
		assertThat(resultedOrder.getOrderStatus()).isEqualTo(OrderStatus.TERMINATED);
		assertThat(resultedOrder.getCommandStatus()).isEqualTo(CommandStatus.ACK);
		assertThat(resultedOrder.getId()).isNotNull().isGreaterThan(1);
		assertThat(resultedOrder.getExecutedQuantity()).isEqualTo(qty);
		assertThat(resultedOrder.getExecPrice()).isEqualTo(price);

		// AND: executed order is into depth and there is no other order
		Order executedOrder = result.getExecutedOrders().get(0);
		assertThat(depth.getAllExecutions()).isNotEmpty().hasSize(2).containsOnly(result.getExecutions().get(0), result.getExecutions().get(1));
		assertThat(depth.getAllOrders()).containsOnly(executedOrder, resultedOrder);
		assertThat(depth.getAllOrders(user)).containsOnly(executedOrder, resultedOrder);
		assertThat(depth.getAllActiveOrders()).containsOnly(executedOrder);
		assertThat(depth.getAllActiveOrders(user)).containsOnly(executedOrder);

		// AND: there is only 1 sell depth
		Summary summary = depth.toSummary();
		assertThat(summary).isNotNull();
		assertThat(summary.getBuyDepthsCount()).isZero();
		assertThat(summary.getSellDepthsCount()).isEqualTo(1);

		// AND: first depth contains executed order
		assertThat(summary.getSellDepths(0).getPrice()).isEqualTo(price);
		assertThat(summary.getSellDepths(0).getQuantity()).isEqualTo(initialQty - qty);
	}

	@Test
	public void shouldInsertSmallSellOrderWithExec() {
		// GIVEN: depth contains big buy order
		int initialQty = 999;
		double price = 123.123;
		Order initialOrder = OrderFactory.create(price, initialQty, BuySell.BUY).build();
		Order initialOrderInserted = depth.insert(initialOrder).getResultedOrder();

		// AND: a sell order with same price and small quantity has to be inserted
		int qty = 5;
		BuySell side = BuySell.SELL;
		Order order = OrderFactory.create(price, qty, side).build();

		// WHEN: insert order
		InsertionResult result = depth.insert(order);

		// THEN: result contains resultedOrder, 2 exec, and 1 executed order
		Order resultedOrder = result.getResultedOrder();
		assertThat(resultedOrder).isNotNull();
		assertThat(result.hasExecutedOrders()).isTrue();
		assertThat(result.getExecutedOrders()).hasSize(1);
		assertThat(result.hasExecutions()).isTrue();
		assertThat(result.getExecutions()).hasSize(2);

		// AND: resulted order is "same" than one inserted
		assertThat(resultedOrder.getInstrument()).isEqualTo(instr);
		assertThat(resultedOrder.getInitialQuantity()).isEqualTo(qty);
		assertThat(resultedOrder.getSide()).isEqualTo(side);
		assertThat(resultedOrder.getPrice()).isEqualTo(price);
		assertThat(resultedOrder.getUserData()).isEqualTo(order.getUserData());

		// AND: resulted order has been correctly updated
		assertThat(resultedOrder.getOrderStatus()).isEqualTo(OrderStatus.TERMINATED);
		assertThat(resultedOrder.getCommandStatus()).isEqualTo(CommandStatus.ACK);
		assertThat(resultedOrder.getId()).isNotNull().isGreaterThan(1);
		assertThat(resultedOrder.getExecutedQuantity()).isEqualTo(qty);
		assertThat(resultedOrder.getExecPrice()).isEqualTo(price);

		// AND: executed order is into depth and there is no other order
		Order executedOrder = result.getExecutedOrders().get(0);
		assertThat(depth.getAllExecutions()).isNotEmpty().hasSize(2).containsOnly(result.getExecutions().get(0), result.getExecutions().get(1));
		assertThat(depth.getAllOrders()).containsOnly(executedOrder, resultedOrder);
		assertThat(depth.getAllOrders(user)).containsOnly(executedOrder, resultedOrder);
		assertThat(depth.getAllActiveOrders()).containsOnly(executedOrder);
		assertThat(depth.getAllActiveOrders(user)).containsOnly(executedOrder);

		// AND: there is only 1 buy depth
		Summary summary = depth.toSummary();
		assertThat(summary).isNotNull();
		assertThat(summary.getSellDepthsCount()).isZero();
		assertThat(summary.getBuyDepthsCount()).isEqualTo(1);

		// AND: first depth contains executed order
		assertThat(summary.getBuyDepths(0).getPrice()).isEqualTo(price);
		assertThat(summary.getBuyDepths(0).getQuantity()).isEqualTo(initialQty - qty);
	}

	// ////////////////////////////////////////////

	@Test
	public void shouldInsertBuyOrderWithExecIntoSellDepthWithManyOrders() {
		// GIVEN: depth contains 3 small sell orders
		int qtySell1 = 3;
		double priceSell1 = 410.0;
		Order orderSell1 = OrderFactory.create(priceSell1, qtySell1, BuySell.SELL).build();
		depth.insert(orderSell1);

		int qtySell2 = 5;
		double priceSell2 = 420.0;
		Order orderSell2 = OrderFactory.create(priceSell2, qtySell2, BuySell.SELL).build();
		depth.insert(orderSell2);

		int qtySell3 = 7;
		double priceSell3 = 430.0;
		Order orderSell3 = OrderFactory.create(priceSell3, qtySell3, BuySell.SELL).build();
		depth.insert(orderSell3);

		// AND: depth contains a big order in last limit
		int qtySell4 = 999;
		double priceSell4 = 440.0;
		Order orderSell4 = OrderFactory.create(priceSell4, qtySell4, BuySell.SELL).build();
		Order orderSell4Inserted = depth.insert(orderSell4).getResultedOrder();

		// AND: a buy order with price between 3 and 4 th limits has to be inserted
		int qty = 500;
		double price = 435.0;
		BuySell side = BuySell.BUY;
		Order order = OrderFactory.create(price, qty, side).build();

		// WHEN: insert order
		InsertionResult result = depth.insert(order);

		// THEN: result contains resultedOrder, 6 exec, and 3 executed order
		Order resultedOrder = result.getResultedOrder();
		assertThat(resultedOrder).isNotNull();
		assertThat(result.hasExecutedOrders()).isTrue();
		assertThat(result.getExecutedOrders()).hasSize(3);
		assertThat(result.hasExecutions()).isTrue();
		assertThat(result.getExecutions()).hasSize(6);

		// AND: resulted order is "same" than one inserted
		assertThat(resultedOrder.getInstrument()).isEqualTo(instr);
		assertThat(resultedOrder.getInitialQuantity()).isEqualTo(qty);
		assertThat(resultedOrder.getSide()).isEqualTo(side);
		assertThat(resultedOrder.getPrice()).isEqualTo(price);
		assertThat(resultedOrder.getUserData()).isEqualTo(order.getUserData());

		// AND: resulted order has been correctly updated
		assertThat(resultedOrder.getOrderStatus()).isEqualTo(OrderStatus.ON_MARKET);
		assertThat(resultedOrder.getCommandStatus()).isEqualTo(CommandStatus.ACK);
		assertThat(resultedOrder.getId()).isNotNull().isGreaterThan(1);
		assertThat(resultedOrder.getExecutedQuantity()).isEqualTo(qtySell1 + qtySell2 + qtySell3);
		double expectedExecPrice = (priceSell1 * qtySell1 + priceSell2 * qtySell2 + priceSell3 * qtySell3) / (qtySell1 + qtySell2 + qtySell3);
		assertThat(resultedOrder.getExecPrice()).isEqualTo(expectedExecPrice); // avg exec price of 3 sell legs

		// AND: executed order is into sell depth and inserted order is into buy depth
		assertThat(depth.getAllExecutions()).isNotEmpty().hasSize(6);
		assertThat(depth.getAllOrders()).hasSize(5).contains(orderSell4Inserted).contains(resultedOrder)
			.contains(result.getExecutedOrders().toArray(new Order[] {}));
		assertThat(depth.getAllOrders(user)).hasSize(5).contains(orderSell4Inserted).contains(resultedOrder)
			.contains(result.getExecutedOrders().toArray(new Order[] {}));
		assertThat(depth.getAllActiveOrders()).hasSize(2);
		assertThat(depth.getAllActiveOrders(user)).hasSize(2);

		// AND: there is only 1 buy and 1 sell depth
		Summary summary = depth.toSummary();
		assertThat(summary).isNotNull();
		assertThat(summary.getSellDepthsCount()).isEqualTo(1);
		assertThat(summary.getBuyDepthsCount()).isEqualTo(1);

		// AND: first buy depth contains resulted order
		assertThat(summary.getBuyDepths(0).getPrice()).isEqualTo(price);
		assertThat(summary.getBuyDepths(0).getQuantity()).isEqualTo(qty - qtySell1 - qtySell2 - qtySell3);

		// AND: first sell depth contains sell order 4
		assertThat(summary.getSellDepths(0).getPrice()).isEqualTo(priceSell4);
		assertThat(summary.getSellDepths(0).getQuantity()).isEqualTo(qtySell4);
	}

	@Test
	public void shouldInsertSellOrderWithExecIntoBuyDepthWithManyOrders() {
		// GIVEN: depth contains 3 small buy orders
		int qtyBuy1 = 3;
		double priceBuy1 = 440.0;
		Order orderBuy1 = OrderFactory.create(priceBuy1, qtyBuy1, BuySell.BUY).build();
		depth.insert(orderBuy1);

		int qtyBuy2 = 5;
		double priceBuy2 = 430.0;
		Order orderBuy2 = OrderFactory.create(priceBuy2, qtyBuy2, BuySell.BUY).build();
		depth.insert(orderBuy2);

		int qtyBuy3 = 7;
		double priceBuy3 = 420.0;
		Order orderBuy3 = OrderFactory.create(priceBuy3, qtyBuy3, BuySell.BUY).build();
		depth.insert(orderBuy3);

		// AND: depth contains a big order in last limit
		int qtyBuy4 = 999;
		double priceBuy4 = 410.0;
		Order orderBuy4 = OrderFactory.create(priceBuy4, qtyBuy4, BuySell.BUY).build();
		Order orderBuy4Inserted = depth.insert(orderBuy4).getResultedOrder();

		// AND: a sell order with price between 3 and 4 th limits has to be inserted
		int qty = 500;
		double price = 415.0;
		BuySell side = BuySell.SELL;
		Order order = OrderFactory.create(price, qty, side).build();

		// WHEN: insert order
		InsertionResult result = depth.insert(order);

		// THEN: result contains resultedOrder, 6 exec, and 3 executed order
		Order resultedOrder = result.getResultedOrder();
		assertThat(resultedOrder).isNotNull();
		assertThat(result.hasExecutedOrders()).isTrue();
		assertThat(result.getExecutedOrders()).hasSize(3);
		assertThat(result.hasExecutions()).isTrue();
		assertThat(result.getExecutions()).hasSize(6);

		// AND: resulted order is "same" than one inserted
		assertThat(resultedOrder.getInstrument()).isEqualTo(instr);
		assertThat(resultedOrder.getInitialQuantity()).isEqualTo(qty);
		assertThat(resultedOrder.getSide()).isEqualTo(side);
		assertThat(resultedOrder.getPrice()).isEqualTo(price);
		assertThat(resultedOrder.getUserData()).isEqualTo(order.getUserData());

		// AND: resulted order has been correctly updated
		assertThat(resultedOrder.getOrderStatus()).isEqualTo(OrderStatus.ON_MARKET);
		assertThat(resultedOrder.getCommandStatus()).isEqualTo(CommandStatus.ACK);
		assertThat(resultedOrder.getId()).isNotNull().isGreaterThan(1);
		assertThat(resultedOrder.getExecutedQuantity()).isEqualTo(qtyBuy1 + qtyBuy2 + qtyBuy3);
		double expectedExecPrice = (priceBuy1 * qtyBuy1 + priceBuy2 * qtyBuy2 + priceBuy3 * qtyBuy3) / (qtyBuy1 + qtyBuy2 + qtyBuy3);
		assertThat(resultedOrder.getExecPrice()).isEqualTo(expectedExecPrice); // avg exec price of 3 sell legs

		// AND: executed order is into buy depth and inserted order is into sell depth
		assertThat(depth.getAllExecutions()).isNotEmpty().hasSize(6);
		assertThat(depth.getAllOrders()).hasSize(5).contains(orderBuy4Inserted).contains(resultedOrder)
			.contains(result.getExecutedOrders().toArray(new Order[] {}));
		assertThat(depth.getAllOrders(user)).hasSize(5).contains(orderBuy4Inserted).contains(resultedOrder)
			.contains(result.getExecutedOrders().toArray(new Order[] {}));
		assertThat(depth.getAllActiveOrders()).hasSize(2);
		assertThat(depth.getAllActiveOrders(user)).hasSize(2);

		// AND: there is only 1 buy and 1 sell depth
		Summary summary = depth.toSummary();
		assertThat(summary).isNotNull();
		assertThat(summary.getSellDepthsCount()).isEqualTo(1);
		assertThat(summary.getBuyDepthsCount()).isEqualTo(1);

		// AND: first sell depth contains resulted order
		assertThat(summary.getSellDepths(0).getPrice()).isEqualTo(price);
		assertThat(summary.getSellDepths(0).getQuantity()).isEqualTo(qty - qtyBuy1 - qtyBuy2 - qtyBuy3);

		// AND: first buy depth contains nuy order 4
		assertThat(summary.getBuyDepths(0).getPrice()).isEqualTo(priceBuy4);
		assertThat(summary.getBuyDepths(0).getQuantity()).isEqualTo(qtyBuy4);
	}

}
