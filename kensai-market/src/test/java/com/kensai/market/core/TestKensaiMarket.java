package com.kensai.market.core;

import static com.google.common.collect.Lists.newArrayList;
import static com.kensai.market.factories.DatasUtil.UNKNOW_USER;
import static com.kensai.market.factories.DatasUtil.USER;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.jboss.netty.channel.Channel;
import org.junit.Before;
import org.junit.Test;

import com.kensai.market.IdGenerator;
import com.kensai.market.factories.DatasUtil;
import com.kensai.market.factories.ExecutionFactory;
import com.kensai.market.factories.OrderFactory;
import com.kensai.market.factories.SummaryFactory;
import com.kensai.market.io.KensaiMessageSender;
import com.kensai.protocol.Trading.BuySell;
import com.kensai.protocol.Trading.CommandStatus;
import com.kensai.protocol.Trading.Execution;
import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.OrderAction;
import com.kensai.protocol.Trading.SubscribeCommand;
import com.kensai.protocol.Trading.Summary;
import com.kensai.protocol.Trading.UnsubscribeCommand;

public class TestKensaiMarket {

	private Instrument instr = DatasUtil.INSTRUMENT;
	private InstrumentDepth depth;

	private Channel channel;
	private KensaiMessageSender sender;

	private KensaiMarket market;

	@Before
	public void init() {
		sender = mock(KensaiMessageSender.class);
		channel = mock(Channel.class);

		depth = mock(InstrumentDepth.class);
		when(depth.getInstrument()).thenReturn(instr);

		market = new KensaiMarket(sender, newArrayList(depth));
	}

	@Test
	public void should_receive_subscribe_add_user_and_send_snapshots() {
		// GIVEN: User has not been added
		when(sender.contains(eq(USER))).thenReturn(false);

		// AND: subscribe command
		SubscribeCommand cmd = SubscribeCommand.newBuilder().setUser(USER).build();

		// WHEN: receivedSubscribe
		market.receivedSubscribe(cmd, channel);

		// THEN: User is add
		verify(sender).addUser(eq(USER), eq(channel), eq(cmd));

		// AND: snapshots are sent
		verify(sender).sendInstrumentsSnapshot(eq(USER), anyList());
		verify(sender).sendOrdersSnapshot(eq(USER), anyList());
		verify(sender).sendExecutionsSnapshot(eq(USER), anyList());
		verify(sender).sendSummariesSnapshot(eq(USER), anyList());
	}

	@Test
	public void shouldReceiveUnsubscribeSendAckWhenUserIsAlreadySubscribed() {
		// GIVEN: User is already subscribed
		when(sender.contains(eq(USER))).thenReturn(true);

		// AND: unsubscribe command with same user
		UnsubscribeCommand cmd = UnsubscribeCommand.newBuilder().setUser(USER).build();

		// WHEN: receivedUnsubscribed
		market.receivedUnsubscribed(cmd, channel);

		// THEN: user is removed
		verify(sender).removeUser(eq(USER), eq(cmd), eq(channel));

		// AND: user is not add
		verifyNoMoreInteractions(sender);
	}

	@Test
	public void shouldReceiveUnsubscribeDoNothingWhenCommandIsNull() {
		// WHEN: receivedUnsubscribed on null command
		market.receivedUnsubscribed(null, channel);

		// THEN: sender do nothing
		verifyNoMoreInteractions(sender);
	}

	@Test
	public void shouldInsertDoNothingWhenOrderIsNull() {
		// WHEN: receivedOrder on null order
		market.receivedOrder(null, channel);

		// THEN: sender do nothing
		verifyNoMoreInteractions(sender);
	}

	@Test
	public void shouldInsertSendNackWhenUserIsUnknow() {
		// GIVEN: an order with an unknown user
		Order order = OrderFactory.create(123.456, 789, BuySell.BUY).setUser(UNKNOW_USER).build();

		// WHEN: receivedOrder
		market.receivedOrder(order, channel);

		// THEN: sender check this user and send NAK
		verify(sender).contains(eq(UNKNOW_USER));
		verify(sender).sendNack(eq(order), eq(channel), anyString());

		// AND: Nothing else is more
		verifyNoMoreInteractions(sender);
	}

	@Test
	public void shouldInsertUpdateDepthAndNotifyForModifications() {
		// GIVEN: user is registered
		when(sender.contains(eq(USER))).thenReturn(true);

		// AND: an order
		Order order = OrderFactory.create(123.456, 789, BuySell.BUY).build();

		// AND: insert in depth will generate order, executions and executedOrders
		Order insertedOrder = Order.newBuilder(order).setCommandStatus(CommandStatus.ACK).build();
		List<Order> execOrders = newArrayList(OrderFactory.create(123.456, 7, BuySell.SELL).build());
		List<Execution> executions = newArrayList(ExecutionFactory.create(order).build(), ExecutionFactory.create(execOrders.get(0)).build());
		when(depth.insert(eq(order))).thenReturn(new InsertionResult(insertedOrder, execOrders, executions));

		// AND: depth return a summary
		Summary insertedSummary = SummaryFactory.create().build();
		when(depth.toSummary()).thenReturn(insertedSummary);

		// WHEN: receivedOrder
		market.receivedOrder(order, channel);

		// THEN: sender check this user, send ACK and send summary for this instrument
		verify(sender).contains(eq(USER));

		// AND: send ACK for inserted order
		verify(sender).send(eq(insertedOrder));

		// AND: notify that summary has changed
		verify(sender).send(eq(insertedSummary));

		// AND: notify for executions and executions order
		verify(sender).send(eq(execOrders.get(0)));
		verify(sender).send(eq(executions.get(0)));
		verify(sender).send(eq(executions.get(1)));

		// AND: do nothing
		verifyNoMoreInteractions(sender);

		// AND: instrument depth has inserted this order
		verify(depth).insert(eq(order));
		verify(depth).toSummary();
		verify(depth, times(2)).getInstrument();
		verifyNoMoreInteractions(depth);
	}

	@Test
	public void shouldUpdateSendNackWhenUserIsUnknow() {
		// GIVEN: an order with an unknown user
		Order order = OrderFactory.create(123.456, 789, BuySell.BUY).setUser(UNKNOW_USER).setAction(OrderAction.UPDATE).build();

		// WHEN: receivedOrder
		market.receivedOrder(order, channel);

		// THEN: sender check this user and send NAK
		verify(sender).contains(eq(UNKNOW_USER));
		verify(sender).sendNack(eq(order), eq(channel), anyString());

		// AND: Nothing else is more
		verifyNoMoreInteractions(sender);
	}

	@Test
	public void shouldUpdateSendNackWhenOrderIsIsInvalid() {
		// GIVEN: an order with no Id
		Order order = OrderFactory.create(123.456, 789, BuySell.BUY).setAction(OrderAction.UPDATE).build();

		// WHEN: receivedOrder
		market.receivedOrder(order, channel);

		// THEN: sender check this user and send NAK
		verify(sender).contains(eq(USER));
		verify(sender).sendNack(eq(order), eq(channel), anyString());

		// AND: Nothing else is more
		verifyNoMoreInteractions(sender);
	}

	@Test
	public void shouldUpdateUpdateDepthAndNotifyForModifications() {
		// GIVEN: user is registered
		when(sender.contains(eq(USER))).thenReturn(true);

		// AND: an order
		long orderId = IdGenerator.generateId();
		Order order = OrderFactory.create(123.456, 789, BuySell.BUY).setAction(OrderAction.UPDATE).setId(orderId).build();

		// AND: depth contains this id
		when(depth.hasOrder(eq(orderId))).thenReturn(true);

		// AND: update in depth will generate order, executions and executedOrders
		Order updatedOrder = Order.newBuilder(order).setCommandStatus(CommandStatus.ACK).build();
		List<Order> execOrders = newArrayList(OrderFactory.create(123.456, 7, BuySell.SELL).build());
		List<Execution> executions = newArrayList(ExecutionFactory.create(order).build(), ExecutionFactory.create(execOrders.get(0)).build());
		when(depth.update(eq(order))).thenReturn(new InsertionResult(updatedOrder, execOrders, executions));

		// AND: depth return a summary
		Summary insertedSummary = SummaryFactory.create().build();
		when(depth.toSummary()).thenReturn(insertedSummary);

		// WHEN: receivedOrder
		market.receivedOrder(order, channel);

		// THEN: sender check this user, send ACK and send summary for this instrument
		verify(sender).contains(eq(USER));

		// AND: send ACK for updated order
		verify(sender).send(eq(updatedOrder));

		// AND: notify that summary has changed
		verify(sender).send(eq(insertedSummary));

		// AND: notify for executions and executions order
		verify(sender).send(eq(execOrders.get(0)));
		verify(sender).send(eq(executions.get(0)));
		verify(sender).send(eq(executions.get(1)));

		// AND: do nothing
		verifyNoMoreInteractions(sender);

		// AND: instrument depth has updated this order
		verify(depth).update(eq(order));
		verify(depth).toSummary();
		verify(depth).hasOrder(eq(orderId));
		verify(depth, times(2)).getInstrument();
		verifyNoMoreInteractions(depth);
	}

	@Test
	public void shouldDeleteSendNackWhenUserIsUnknow() {
		// GIVEN: an order with an unknown user
		Order order = OrderFactory.create(123.456, 789, BuySell.BUY).setUser(UNKNOW_USER).setAction(OrderAction.DELETE).build();

		// WHEN: receivedOrder
		market.receivedOrder(order, channel);

		// THEN: sender check this user and send NAK
		verify(sender).contains(eq(UNKNOW_USER));
		verify(sender).sendNack(eq(order), eq(channel), anyString());

		// AND: Nothing else is more
		verifyNoMoreInteractions(sender);
	}

	@Test
	public void shouldDeleteSendNackWhenOrderIsIsInvalid() {
		// GIVEN: an order with no Id
		Order order = OrderFactory.create(123.456, 789, BuySell.BUY).setAction(OrderAction.DELETE).build();

		// WHEN: receivedOrder
		market.receivedOrder(order, channel);

		// THEN: sender check this user and send NAK
		verify(sender).contains(eq(USER));
		verify(sender).sendNack(eq(order), eq(channel), anyString());

		// AND: Nothing else is more
		verifyNoMoreInteractions(sender);
	}

	@Test
	public void shouldDeleteUpdateDepthAndNotifyForModifications() {
		// GIVEN: user is registered
		when(sender.contains(eq(USER))).thenReturn(true);

		// AND: an order
		long orderId = IdGenerator.generateId();
		Order order = OrderFactory.create(123.456, 789, BuySell.BUY).setAction(OrderAction.DELETE).setId(orderId).build();

		// AND: depth contains this id
		when(depth.hasOrder(eq(orderId))).thenReturn(true);

		// AND: update in depth will generate order, executions and executedOrders
		Order updatedOrder = Order.newBuilder(order).setCommandStatus(CommandStatus.ACK).build();
		List<Order> execOrders = newArrayList(OrderFactory.create(123.456, 7, BuySell.SELL).build());
		List<Execution> executions = newArrayList(ExecutionFactory.create(order).build(), ExecutionFactory.create(execOrders.get(0)).build());
		when(depth.delete(eq(order))).thenReturn(new InsertionResult(updatedOrder, execOrders, executions));

		// AND: depth return a summary
		Summary insertedSummary = SummaryFactory.create().build();
		when(depth.toSummary()).thenReturn(insertedSummary);

		// WHEN: receivedOrder
		market.receivedOrder(order, channel);

		// THEN: sender check this user, send ACK and send summary for this instrument
		verify(sender).contains(eq(USER));

		// AND: send ACK for updated order
		verify(sender).send(eq(updatedOrder));

		// AND: notify that summary has changed
		verify(sender).send(eq(insertedSummary));

		// AND: notify for executions and executions order
		verify(sender).send(eq(execOrders.get(0)));
		verify(sender).send(eq(executions.get(0)));
		verify(sender).send(eq(executions.get(1)));

		// AND: do nothing
		verifyNoMoreInteractions(sender);

		// AND: instrument depth has deleted this order
		verify(depth).delete(eq(order));
		verify(depth).toSummary();
		verify(depth).hasOrder(eq(orderId));
		verify(depth, times(2)).getInstrument();
		verifyNoMoreInteractions(depth);
	}

	@Test
	public void should_send_nack_when_order_has_negative_price() {
		// GIVEN: Order with negative price
		Order order = OrderFactory.create(-1.0, 123, BuySell.BUY).build();

		// WHEN: receive order
		market.receivedOrder(order, channel);

		// THEN: Send nack
		verify(sender).sendNack(eq(order), eq(channel), anyString());

		// AND: do not send summary for this
		verify(sender, never()).send(any(Summary.class));
	}

	@Test
	public void should_send_nack_when_order_has_zero_price() {
		// GIVEN: Order with negative price
		Order order = OrderFactory.create(0.0, 123, BuySell.BUY).build();

		// WHEN: receive order
		market.receivedOrder(order, channel);

		// THEN: Send nack
		verify(sender).sendNack(eq(order), eq(channel), anyString());

		// AND: do not send summary for this
		verify(sender, never()).send(any(Summary.class));
	}

	@Test
	public void should_send_nack_when_order_has_negative_qty() {
		// GIVEN: Order with negative qty
		Order order = OrderFactory.create(123.0, -123, BuySell.BUY).build();

		// WHEN: receive order
		market.receivedOrder(order, channel);

		// THEN: Send nack
		verify(sender).sendNack(eq(order), eq(channel), anyString());

		// AND: do not send summary for this
		verify(sender, never()).send(any(Summary.class));
	}

	@Test
	public void should_send_nack_when_order_has_zero_qty() {
		// GIVEN: Order with zero qty
		Order order = OrderFactory.create(123.0, 0, BuySell.BUY).build();

		// WHEN: receive order
		market.receivedOrder(order, channel);

		// THEN: Send nack
		verify(sender).sendNack(eq(order), eq(channel), anyString());

		// AND: do not send summary for this
		verify(sender, never()).send(any(Summary.class));
	}
}
