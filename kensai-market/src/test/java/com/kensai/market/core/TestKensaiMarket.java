package com.kensai.market.core;

import static com.google.common.collect.Lists.newArrayList;
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

	private String user = OrderFactory.USER;

	private Instrument instr = OrderFactory.INSTRUMENT;
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

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReceiveSubscribeSendAckWhenUserDoesNotExist() {
		// GIVEN: User has not been added
		when(sender.contains(eq(user))).thenReturn(false);

		// AND: subscribe command
		SubscribeCommand cmd = SubscribeCommand.newBuilder().setUser(user).build();

		// WHEN: receivedSubscribe
		market.receivedSubscribe(cmd, channel);

		// THEN: User is add
		verify(sender).addUser(eq(user), eq(channel));

		// AND: ACK is sent
		verify(sender).sendAck(eq(cmd), eq(channel));

		// AND: snapshots are sent
		verify(sender).sendInstrumentsSnapshot(eq(user), anyList());
		verify(sender).sendOrdersSnapshot(eq(user), anyList());
		verify(sender).sendExecutionsSnapshot(eq(user), anyList());
		verify(sender).sendSummariesSnapshot(eq(user), anyList());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReceiveSubscribeSendNackWhenUserIsAlreadySubscribedAndSendSnapshot() {
		// GIVEN: User is already subscribed
		when(sender.contains(eq(user))).thenReturn(true);

		// AND: subscribe command with same user
		SubscribeCommand cmd = SubscribeCommand.newBuilder().setUser(user).build();

		// WHEN: receivedSubscribe
		market.receivedSubscribe(cmd, channel);

		// THEN: NAK is sent
		verify(sender).sendNack(eq(cmd), eq(channel), anyString());

		// AND: user is updated
		verify(sender).addUser(eq(user), eq(channel));

		// AND: snapshot are sent
		verify(sender).sendInstrumentsSnapshot(eq(user), anyList());
		verify(sender).sendOrdersSnapshot(eq(user), anyList());
		verify(sender).sendExecutionsSnapshot(eq(user), anyList());
		verify(sender).sendSummariesSnapshot(eq(user), anyList());
	}

	@Test
	public void shouldReceiveSubscribeDoNothingWhenCommandIsNull() {
		// WHEN: receivedSubscribe on null command
		market.receivedSubscribe(null, channel);

		// THEN: sender do nothing
		verify(sender, never()).sendAck(any(SubscribeCommand.class), eq(channel));
	}

	@Test
	public void shouldReceiveUnsubscribeSendNackWhenUserDoesNotExist() {
		// GIVEN: User has not been added
		when(sender.contains(eq(user))).thenReturn(false);

		// AND: unsubscribe command
		UnsubscribeCommand cmd = UnsubscribeCommand.newBuilder().setUser(user).build();

		// WHEN: receivedUnsubscribed
		market.receivedUnsubscribed(cmd, channel);

		// THEN: User is not add
		verify(sender, never()).addUser(eq(user), eq(channel));

		// AND: NAK is sent
		verify(sender).sendNack(eq(cmd), eq(channel), anyString());
	}

	@Test
	public void shouldReceiveUnsubscribeSendAckWhenUserIsAlreadySubscribed() {
		// GIVEN: User is already subscribed
		when(sender.contains(eq(user))).thenReturn(true);

		// AND: unsubscribe command with same user
		UnsubscribeCommand cmd = UnsubscribeCommand.newBuilder().setUser(user).build();

		// WHEN: receivedUnsubscribed
		market.receivedUnsubscribed(cmd, channel);

		// THEN: user has been check
		verify(sender).contains(eq(user));

		// AND: ACK is sent
		verify(sender).sendAck(eq(cmd), eq(channel));

		// AND: user is removed
		verify(sender).removeUser(eq(user));

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
		String unknownUser = user + " is unknow";
		Order order = OrderFactory.create(123.456, 789, BuySell.BUY).setUser(unknownUser).build();

		// WHEN: receivedOrder
		market.receivedOrder(order, channel);

		// THEN: sender check this user and send NAK
		verify(sender).contains(eq(unknownUser));
		verify(sender).sendNack(eq(order), eq(channel), anyString());

		// AND: Nothing else is more
		verifyNoMoreInteractions(sender);
	}

	@Test
	public void shouldInsertUpdateDepthAndNotifyForModifications() {
		// GIVEN: user is registered
		when(sender.contains(eq(user))).thenReturn(true);

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
		verify(sender).contains(eq(user));

		// AND: send ACK for inserted order
		verify(sender).send(eq(insertedOrder), eq(channel));

		// AND: notify that summary has changed
		verify(sender).send(eq(insertedSummary));

		// AND: notify for executions and executions order
		verify(sender).send(eq(execOrders.get(0)), eq(channel));
		verify(sender).send(eq(executions.get(0)), eq(channel));
		verify(sender).send(eq(executions.get(1)), eq(channel));

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
		String unknownUser = user + " is unknow";
		Order order = OrderFactory.create(123.456, 789, BuySell.BUY).setUser(unknownUser).setAction(OrderAction.UPDATE).build();

		// WHEN: receivedOrder
		market.receivedOrder(order, channel);

		// THEN: sender check this user and send NAK
		verify(sender).contains(eq(unknownUser));
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
		verify(sender).contains(eq(user));
		verify(sender).sendNack(eq(order), eq(channel), anyString());

		// AND: Nothing else is more
		verifyNoMoreInteractions(sender);
	}

	@Test
	public void shouldUpdateUpdateDepthAndNotifyForModifications() {
		// GIVEN: user is registered
		when(sender.contains(eq(user))).thenReturn(true);

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
		verify(sender).contains(eq(user));

		// AND: send ACK for updated order
		verify(sender).send(eq(updatedOrder), eq(channel));

		// AND: notify that summary has changed
		verify(sender).send(eq(insertedSummary));

		// AND: notify for executions and executions order
		verify(sender).send(eq(execOrders.get(0)), eq(channel));
		verify(sender).send(eq(executions.get(0)), eq(channel));
		verify(sender).send(eq(executions.get(1)), eq(channel));

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
		String unknownUser = user + " is unknow";
		Order order = OrderFactory.create(123.456, 789, BuySell.BUY).setUser(unknownUser).setAction(OrderAction.DELETE).build();

		// WHEN: receivedOrder
		market.receivedOrder(order, channel);

		// THEN: sender check this user and send NAK
		verify(sender).contains(eq(unknownUser));
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
		verify(sender).contains(eq(user));
		verify(sender).sendNack(eq(order), eq(channel), anyString());

		// AND: Nothing else is more
		verifyNoMoreInteractions(sender);
	}

	@Test
	public void shouldDeleteUpdateDepthAndNotifyForModifications() {
		// GIVEN: user is registered
		when(sender.contains(eq(user))).thenReturn(true);

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
		verify(sender).contains(eq(user));

		// AND: send ACK for updated order
		verify(sender).send(eq(updatedOrder), eq(channel));

		// AND: notify that summary has changed
		verify(sender).send(eq(insertedSummary));

		// AND: notify for executions and executions order
		verify(sender).send(eq(execOrders.get(0)), eq(channel));
		verify(sender).send(eq(executions.get(0)), eq(channel));
		verify(sender).send(eq(executions.get(1)), eq(channel));

		// AND: do nothing
		verifyNoMoreInteractions(sender);

		// AND: instrument depth has deleted this order
		verify(depth).delete(eq(order));
		verify(depth).toSummary();
		verify(depth).hasOrder(eq(orderId));
		verify(depth, times(2)).getInstrument();
		verifyNoMoreInteractions(depth);
	}
}
