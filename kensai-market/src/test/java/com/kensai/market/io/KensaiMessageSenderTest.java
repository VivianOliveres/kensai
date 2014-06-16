package com.kensai.market.io;

import static com.google.common.collect.Lists.newArrayList;
import static com.kensai.market.factories.DatasUtil.UNKNOW_USER;
import static com.kensai.market.factories.DatasUtil.USER;
import static com.kensai.market.factories.DatasUtil.USER_LISTENER;
import static com.kensai.market.factories.DatasUtil.USER_UNLISTENER;
import static com.kensai.protocol.Trading.BuySell.BUY;
import static com.kensai.protocol.Trading.Role.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.channel.Channel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.kensai.market.core.UserCredentials;
import com.kensai.market.factories.DatasUtil;
import com.kensai.market.factories.ExecutionFactory;
import com.kensai.market.factories.OrderFactory;
import com.kensai.market.factories.SummaryFactory;
import com.kensai.market.matchers.ExecMatcher;
import com.kensai.market.matchers.ExecutionsSnapshotMatcher;
import com.kensai.market.matchers.InstrumentsSnapshotMatcher;
import com.kensai.market.matchers.OrderMatcher;
import com.kensai.market.matchers.OrdersSnapshotMatcher;
import com.kensai.market.matchers.SubscribeCmdMatcher;
import com.kensai.market.matchers.SummariesSnapshotMatcher;
import com.kensai.market.matchers.SummaryMatcher;
import com.kensai.market.matchers.UnsubscribeCmdMatcher;
import com.kensai.protocol.Trading.CommandStatus;
import com.kensai.protocol.Trading.Execution;
import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.Role;
import com.kensai.protocol.Trading.SubscribeCommand;
import com.kensai.protocol.Trading.Summary;
import com.kensai.protocol.Trading.UnsubscribeCommand;
import com.kensai.protocol.Trading.User;

@RunWith(MockitoJUnitRunner.class)
public class KensaiMessageSenderTest {

	private KensaiMessageSender sender;

	private UserCredentials uc1;
	@Mock private ChannelWritter channel1;

	private UserCredentials uc2;
	@Mock private ChannelWritter channel2;

	private UserCredentials uc4;
	@Mock private ChannelWritter channel4;

	private UserCredentials uc5;
	@Mock private ChannelWritter channel5;

	@Before
	public void init() {
		sender = new KensaiMessageSender();

		uc1 = new UserCredentials(USER, channel1);
		uc2 = new UserCredentials(UNKNOW_USER, channel2);
		uc4 = new UserCredentials(USER_UNLISTENER, channel4);
		uc5 = new UserCredentials(USER_LISTENER, channel5);
	}

	@Test
	public void should_addUser_send_ACK_when_user_does_not_exist() {
		// GIVEN: user does not exist
		assertThat(sender.contains(USER)).isFalse();
		assertThat(sender.contains(uc1)).isFalse();

		// AND: a SubscribeCommand is received
		SubscribeCommand cmd = SubscribeCommand.newBuilder().setUser(USER).build();

		// WHEN: add user
		sender.addUser(uc1, cmd);

		// THEN: user is added
		assertThat(sender.contains(USER)).isTrue();
		assertThat(sender.contains(uc1)).isTrue();

		// AND: ACK has been sent
		verify(channel1).write(argThat(new SubscribeCmdMatcher(CommandStatus.ACK)));
	}

	@Test
	public void should_addUser_send_NACK_when_user_exist() {
		// GIVEN: user that exists
		SubscribeCommand cmd = SubscribeCommand.newBuilder().setUser(USER).build();
		sender.addUser(uc1, cmd);

		assertThat(sender.contains(USER)).isTrue();
		assertThat(sender.contains(uc1)).isTrue();

		// WHEN: add user on a new SubscribeUser
		sender.addUser(uc1, cmd);

		// THEN: NACK has been sent
		verify(channel1).write(argThat(new SubscribeCmdMatcher(CommandStatus.NACK)));
	}

	@Test
	public void should_contains_works_with_multiple_users() {
		// WHEN: Add first user
		SubscribeCommand cmd = SubscribeCommand.newBuilder().setUser(USER).build();
		sender.addUser(uc1, cmd);

		// AND: add another user
		cmd = SubscribeCommand.newBuilder().setUser(UNKNOW_USER).build();
		sender.addUser(uc2, cmd);

		// THEN: contains uc
		assertThat(sender.contains(USER)).isTrue();
		assertThat(sender.contains(uc1)).isTrue();

		// AND: contains uc2
		assertThat(sender.contains(UNKNOW_USER)).isTrue();
		assertThat(sender.contains(uc2)).isTrue();

		// AND: not contains an not added user
		User notUsedUser = User.newBuilder().setName("notUsedUser").setExecListeningRole(Role.ADMIN).setOrderListeningRole(Role.ADMIN)
			.setIsListeningSummary(true).build();
		cmd = SubscribeCommand.newBuilder().setUser(notUsedUser).build();
		UserCredentials uc3 = new UserCredentials(notUsedUser, channel1);

		assertThat(sender.contains(notUsedUser)).isFalse();
		assertThat(sender.contains(uc3)).isFalse();
	}

	@Test
	public void should_getUser_works_with_multiple_users() {
		// WHEN: Add first user
		SubscribeCommand cmd = SubscribeCommand.newBuilder().setUser(USER).build();
		sender.addUser(uc1, cmd);

		// AND: add another user
		cmd = SubscribeCommand.newBuilder().setUser(UNKNOW_USER).build();
		sender.addUser(uc2, cmd);

		// THEN: contains uc
		assertThat(sender.getUser(USER)).isNotNull().isEqualTo(uc1);
		assertThat(sender.getUser(uc1)).isNotNull().isEqualTo(uc1);

		// AND: contains uc2
		assertThat(sender.getUser(UNKNOW_USER)).isNotNull().isEqualTo(uc2);
		assertThat(sender.getUser(uc2)).isNotNull().isEqualTo(uc2);

		// AND: not contains an not added user
		User notUsedUser = User.newBuilder().setName("notUsedUser").setExecListeningRole(ADMIN).setOrderListeningRole(ADMIN)
			.setIsListeningSummary(true).build();
		cmd = SubscribeCommand.newBuilder().setUser(notUsedUser).build();
		UserCredentials uc3 = new UserCredentials(notUsedUser, channel1);

		assertThat(sender.getUser(notUsedUser)).isNull();
		assertThat(sender.getUser(uc3)).isNull();
	}

	@Test
	public void should_removeUser_send_NACK_when_user_does_not_exist() {
		// GIVEN: user does not exist
		assertThat(sender.contains(USER)).isFalse();
		assertThat(sender.contains(uc1)).isFalse();

		// AND: a UnsubscribeCommand is received
		UnsubscribeCommand cmd = UnsubscribeCommand.newBuilder().setUser(USER).build();

		// WHEN: removeUser
		sender.removeUser(uc1, cmd);

		// THEN: user does not exist
		assertThat(sender.contains(USER)).isFalse();
		assertThat(sender.contains(uc1)).isFalse();

		// AND: NACK has been sent
		verify(channel1).write(argThat(new UnsubscribeCmdMatcher(CommandStatus.NACK)));
	}

	@Test
	public void should_removeUser_send_ACK_when_user_exist() {
		// GIVEN: user that exists
		SubscribeCommand subscribeCmd = SubscribeCommand.newBuilder().setUser(USER).build();
		sender.addUser(uc1, subscribeCmd);

		assertThat(sender.contains(USER)).isTrue();
		assertThat(sender.contains(uc1)).isTrue();

		// WHEN: removeUser
		UnsubscribeCommand cmd = UnsubscribeCommand.newBuilder().setUser(USER).build();
		sender.removeUser(uc1, cmd);

		// THEN: user has been removed
		assertThat(sender.contains(USER)).isFalse();
		assertThat(sender.contains(uc1)).isFalse();

		// AND: ACK has been sent
		verify(channel1).write(argThat(new UnsubscribeCmdMatcher(CommandStatus.ACK)));
	}

	@Test
	public void should_sendInstrumentsSnapshot_send_InstrumentsSnapshot() {
		// GIVEN: InstrumentsSnapshot
		List<Instrument> instruments = newArrayList(DatasUtil.INSTRUMENT);

		// AND: user added
		SubscribeCommand cmd = SubscribeCommand.newBuilder().setUser(USER).build();
		sender.addUser(uc1, cmd);

		// WHEN: sendInstrumentsSnapshot
		sender.sendInstrumentsSnapshot(uc1.getUser(), instruments);

		// THEN: InstrumentsSnapshot has been sent
		verify(channel1).write(argThat(new InstrumentsSnapshotMatcher(instruments)));
	}

	@Test
	public void should_sendSummariesSnapshot_send_SummariesSnapshot() {
		// GIVEN: InstrumentsSnapshot
		List<Summary> summaries = newArrayList(SummaryFactory.create().build());

		// AND: user added
		SubscribeCommand cmd = SubscribeCommand.newBuilder().setUser(USER).build();
		sender.addUser(uc1, cmd);

		// WHEN: sendInstrumentsSnapshot
		sender.sendSummariesSnapshot(uc1.getUser(), summaries);

		// THEN: InstrumentsSnapshot has been sent
		verify(channel1).write(argThat(new SummariesSnapshotMatcher(summaries)));
	}

	@Test
	public void should_sendSummariesSnapshot_send_empty_SummariesSnapshot_when_user_is_notListening_summaries() {
		// GIVEN: InstrumentsSnapshot
		List<Summary> summaries = newArrayList(SummaryFactory.create().build());

		// AND: user added
		SubscribeCommand cmd = SubscribeCommand.newBuilder().setUser(USER_UNLISTENER).build();
		sender.addUser(uc4, cmd);

		// WHEN: sendInstrumentsSnapshot
		sender.sendSummariesSnapshot(USER_UNLISTENER, summaries);

		// THEN: InstrumentsSnapshot has been sent with no instruments
		verify(channel4).write(argThat(new SummariesSnapshotMatcher(new ArrayList<>())));
	}

	@Test
	public void should_sendOrdersSnapshot_send_OrdersSnapshot() {
		// GIVEN: OrdersSnapshot
		List<Order> orders = newArrayList(OrderFactory.create(123.456, 789, BUY).build());

		// AND: user added
		SubscribeCommand cmd = SubscribeCommand.newBuilder().setUser(USER).build();
		sender.addUser(uc1, cmd);

		// WHEN: sendInstrumentsSnapshot
		sender.sendOrdersSnapshot(uc1.getUser(), orders);

		// THEN: OrdersSnapshot has been sent
		verify(channel1).write(argThat(new OrdersSnapshotMatcher(orders)));
	}

	@Test
	public void should_sendOrdersSnapshot_send_empty_OrdersSnapshot_when_user_is_notListening() {
		// GIVEN: OrdersSnapshot
		List<Order> orders = newArrayList(OrderFactory.create(123.456, 789, BUY).build());

		// AND: user added
		SubscribeCommand cmd = SubscribeCommand.newBuilder().setUser(USER_UNLISTENER).build();
		sender.addUser(uc4, cmd);

		// WHEN: sendOrdersSnapshot
		sender.sendOrdersSnapshot(USER_UNLISTENER, orders);

		// THEN: OrdersSnapshot has been sent
		verify(channel4).write(argThat(new OrdersSnapshotMatcher(new ArrayList<>())));
	}

	@Test
	public void should_sendExecutionsSnapshot_send_ExecutionsSnapshot() {
		// GIVEN: ExecutionsSnapshot
		Order order = OrderFactory.create(123.456, 789, BUY).build();
		Execution exec = ExecutionFactory.create(order).build();
		List<Execution> execs = newArrayList(exec);

		// AND: user added
		SubscribeCommand cmd = SubscribeCommand.newBuilder().setUser(USER).build();
		sender.addUser(uc1, cmd);

		// WHEN: sendExecutionsSnapshot
		sender.sendExecutionsSnapshot(uc1.getUser(), execs);

		// THEN: OrdersSnapshot has been sent
		verify(channel1).write(argThat(new ExecutionsSnapshotMatcher(execs)));
	}

	@Test
	public void should_sendExecutionsSnapshot_send_empty_ExecutionsSnapshot_when_user_is_notListening() {
		// GIVEN: ExecutionsSnapshot
		Order order = OrderFactory.create(123.456, 789, BUY).build();
		Execution exec = ExecutionFactory.create(order).build();
		List<Execution> execs = newArrayList(exec);

		// AND: user added
		SubscribeCommand cmd = SubscribeCommand.newBuilder().setUser(USER_UNLISTENER).build();
		sender.addUser(uc4, cmd);

		// WHEN: sendExecutionsSnapshot
		sender.sendExecutionsSnapshot(USER_UNLISTENER, execs);

		// THEN: OrdersSnapshot has been sent
		verify(channel4).write(argThat(new ExecutionsSnapshotMatcher(new ArrayList<>())));
	}

	@Test
	public void should_sendOrder_to_listening_user_only() {
		// GIVEN: USER
		SubscribeCommand cmd = SubscribeCommand.newBuilder().setUser(USER).build();
		sender.addUser(uc1, cmd);
		assertThat(sender.contains(USER)).isTrue();
		
		// AND: USER_UNLISTENER
		cmd = SubscribeCommand.newBuilder().setUser(USER_UNLISTENER).build();
		sender.addUser(uc4, cmd);
		assertThat(sender.contains(USER_UNLISTENER)).isTrue();

		// AND: USER_LISTENER
		cmd = SubscribeCommand.newBuilder().setUser(USER_LISTENER).build();
		sender.addUser(uc5, cmd);
		assertThat(sender.contains(USER_LISTENER)).isTrue();
		
		// AND: an order sent by USER
		Order order = OrderFactory.create(123.456, 789, BUY).build();

		// WHEN: sendOrder
		sender.send(order);

		// THEN: order is sent to USER
		verify(channel1).write(argThat(new OrderMatcher(order)));

		// AND: order is never sent to USER_UNLISTENER
		verify(channel4, never()).write(new OrderMatcher());

		// AND: order is sent to USER_LISTENER
		verify(channel5).write(argThat(new OrderMatcher(order)));
	}

	@Test
	public void should_sendExec_to_listening_user_only() {
		// GIVEN: USER
		SubscribeCommand cmd = SubscribeCommand.newBuilder().setUser(USER).build();
		sender.addUser(uc1, cmd);
		assertThat(sender.contains(USER)).isTrue();

		// AND: USER_UNLISTENER
		cmd = SubscribeCommand.newBuilder().setUser(USER_UNLISTENER).build();
		sender.addUser(uc4, cmd);
		assertThat(sender.contains(USER_UNLISTENER)).isTrue();

		// AND: USER_LISTENER
		cmd = SubscribeCommand.newBuilder().setUser(USER_LISTENER).build();
		sender.addUser(uc5, cmd);
		assertThat(sender.contains(USER_LISTENER)).isTrue();

		// AND: an order and exec sent by USER
		Order order = OrderFactory.create(123.456, 789, BUY).build();
		Execution exec = ExecutionFactory.create(order).build();

		// WHEN: sendExec
		sender.send(exec);

		// THEN: exec is sent to USER
		verify(channel1).write(argThat(new ExecMatcher(exec)));

		// AND: exec is never sent to USER_UNLISTENER
		verify(channel4, never()).write(new ExecMatcher());

		// AND: exec is sent to USER_LISTENER
		verify(channel5).write(argThat(new ExecMatcher(exec)));
	}

	@Test
	public void should_sendSummary_to_listening_user_only() {
		// GIVEN: USER
		SubscribeCommand cmd = SubscribeCommand.newBuilder().setUser(USER).build();
		sender.addUser(uc1, cmd);
		assertThat(sender.contains(USER)).isTrue();

		// AND: USER_UNLISTENER
		cmd = SubscribeCommand.newBuilder().setUser(USER_UNLISTENER).build();
		sender.addUser(uc4, cmd);
		assertThat(sender.contains(USER_UNLISTENER)).isTrue();

		// AND: USER_LISTENER
		cmd = SubscribeCommand.newBuilder().setUser(USER_LISTENER).build();
		sender.addUser(uc5, cmd);
		assertThat(sender.contains(USER_LISTENER)).isTrue();

		// AND: a summary sent by USER
		Summary summary = SummaryFactory.create().build();

		// WHEN: sendSummary
		sender.send(summary);

		// THEN: summary is sent to USER
		verify(channel1).write(argThat(new SummaryMatcher(summary)));

		// AND: summary is never sent to USER_UNLISTENER
		verify(channel4, never()).write(new SummaryMatcher());

		// AND: summary is sent to USER_LISTENER
		verify(channel5).write(argThat(new SummaryMatcher(summary)));
	}
}
