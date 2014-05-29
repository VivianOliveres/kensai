package com.kensai.gui.services.connectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutionException;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.junit.Before;
import org.junit.Test;

import com.kensai.gui.AbstractTestJavaFX;
import com.kensai.gui.services.ApplicationContext;
import com.kensai.gui.services.model.ModelService;
import com.kensai.gui.services.model.instruments.InstrumentModel;
import com.kensai.gui.services.model.instruments.InstrumentsModel;
import com.kensai.gui.services.model.market.ConnectionState;
import com.kensai.gui.services.model.market.MarketConnectionModel;
import com.kensai.gui.services.task.TaskService;
import com.kensai.protocol.Trading.Depth;
import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.InstrumentType;
import com.kensai.protocol.Trading.InstrumentsSnapshot;
import com.kensai.protocol.Trading.MarketStatus;
import com.kensai.protocol.Trading.SubscribeCommand;
import com.kensai.protocol.Trading.SummariesSnapshot;
import com.kensai.protocol.Trading.Summary;
import com.kensai.protocol.Trading.UnsubscribeCommand;

public class MarketConnectorTest extends AbstractTestJavaFX {

	private MarketConnectionModel swx = new MarketConnectionModel("SWX", "localhost", 1664, true);
	private InetSocketAddress socketAddress = new InetSocketAddress("localhost", 1664);

	private MarketConnector connector;

	private MarketConnectorMessageSender sender = mock(MarketConnectorMessageSender.class);
	private ClientBootstrap bootstrap = mock(ClientBootstrap.class);
	private TaskService taskService = mock(TaskService.class);
	private InstrumentsModel instrumentsModel = mock(InstrumentsModel.class);

	@Before
	public void init() {
		ApplicationContext context = mock(ApplicationContext.class);
		given(context.getTaskService()).willReturn(taskService);

		ModelService modelService = mock(ModelService.class);
		given(modelService.getInstruments()).willReturn(instrumentsModel);
		given(context.getModelService()).willReturn(modelService);

		connector = new MarketConnector(swx, context, bootstrap, sender);
	}

	@Test
	public void should_connect_do_nothing_when_model_is_already_connected() {
		// GIVEN: model is already connected
		swx.setConnectionState(ConnectionState.CONNECTED);

		// WHEN: connect
		connector.connect();

		// THEN: model is not updated
		assertThat(connector.getConnectionState()).isEqualTo(ConnectionState.CONNECTED);

		// AND: connection is not called
		verify(taskService, never()).runInBackground(any(Runnable.class));
	}

	@Test
	public void should_connect_do_nothing_when_model_is_connecting() {
		// GIVEN: model is already connected
		swx.setConnectionState(ConnectionState.CONNECTING);

		// WHEN: connect
		connector.connect();

		// THEN: model is not updated
		assertThat(connector.getConnectionState()).isEqualTo(ConnectionState.CONNECTING);

		// AND: connection is not called
		verify(taskService, never()).runInBackground(any(Runnable.class));
	}

	@Test
	public void should_connect_in_background() {
		// WHEN: connect
		connector.connect();

		// THEN: GUI is updated
		assertThat(connector.getConnectionState()).isEqualTo(ConnectionState.CONNECTING);

		// AND: connection is done in background
		verify(taskService).runInBackground(any(Runnable.class));
	}

	@Test
	public void should_send_subscribe_msg_when_doConnection_success() {
		// GIVEN: model is connecting
		swx.setConnectionState(ConnectionState.CONNECTING);

		// AND: : connection will success
		ChannelFuture channelFuture = mock(ChannelFuture.class);
		given(channelFuture.isSuccess()).willReturn(true);

		Channel connectedChannel = mock(Channel.class);
		given(channelFuture.getChannel()).willReturn(connectedChannel);

		given(bootstrap.connect(eq(socketAddress))).willReturn(channelFuture);

		// WHEN: connect
		connector.doConnection(socketAddress, 1);

		// THEN: Should delegate connection to bootstrap
		verify(bootstrap).connect(eq(socketAddress));

		// AND: Should send subscribe message
		verify(sender).send(eq(connectedChannel), any(SubscribeCommand.class));
	}

	@Test
	public void should_notify_gui_when_connection_fails_with_timeout() throws InterruptedException, ExecutionException {
		// GIVEN: model is connecting
		swx.setConnectionState(ConnectionState.CONNECTING);

		// AND: connection will produce timeout
		ChannelFuture channelFuture = mock(ChannelFuture.class);
		given(channelFuture.await(anyLong())).willThrow(InterruptedException.class);
		given(bootstrap.connect(eq(socketAddress))).willReturn(channelFuture);

		// WHEN: connect
		connector.doConnection(socketAddress, 1);

		// THEN: Should delegate connection to bootstrap
		verify(bootstrap).connect(eq(socketAddress));

		// AND: has stop channelFuture
		verify(channelFuture).cancel();

		// AND: ConnectionState model is disconnected
		runAndWait(() -> assertThat(swx.getConnectionState()).isEqualTo(ConnectionState.DISCONNECTED));
	}

	@Test
	public void should_notify_gui_when_connection_fails() throws InterruptedException, ExecutionException {
		// GIVEN: model is connecting
		swx.setConnectionState(ConnectionState.CONNECTING);

		// AND: : connection will fail
		ChannelFuture channelFuture = mock(ChannelFuture.class);
		given(channelFuture.isSuccess()).willReturn(false);
		given(bootstrap.connect(eq(socketAddress))).willReturn(channelFuture);

		// WHEN: connect
		connector.doConnection(socketAddress, 1);

		// THEN: Should delegate connection to bootstrap
		verify(bootstrap).connect(eq(socketAddress));

		// AND: ConnectionState model is disconnected
		runAndWait(() -> assertThat(swx.getConnectionState()).isEqualTo(ConnectionState.DISCONNECTED));
	}

	@Test
	public void should_disconnection_do_nothing_when_model_is_disconnected() {
		// GIVEN: model is disconnected
		swx.setConnectionState(ConnectionState.DISCONNECTED);

		// WHEN: disconnect
		connector.disconnect();

		// THEN: model is not updated
		assertThat(swx.getConnectionState()).isEqualTo(ConnectionState.DISCONNECTED);

		// AND: do nothing
		verify(taskService, never()).runInBackground(any(Runnable.class));
	}

	@Test
	public void should_disconnection_update_gui_and_disconnect_in_background() {
		// GIVEN: model is connected
		swx.setConnectionState(ConnectionState.CONNECTED);

		// WHEN: disconnect
		connector.disconnect();

		// THEN: model is updated
		assertThat(swx.getConnectionState()).isEqualTo(ConnectionState.CONNECTING);

		// AND: disconnection is done in background
		verify(taskService).runInBackground(any(Runnable.class));
	}

	@Test
	public void should_doDisconnection_fails_when_disconnection_fails_with_timeout() throws Exception {
		// GIVEN: model is connected
		ChannelFuture connectionChannelFuture = mock(ChannelFuture.class);

		ChannelFuture disconnectionChannelFuture = mock(ChannelFuture.class);
		given(disconnectionChannelFuture.await(anyLong())).willThrow(InterruptedException.class);

		Channel channel = mock(Channel.class);
		given(channel.close()).willReturn(disconnectionChannelFuture);
		given(connectionChannelFuture.getChannel()).willReturn(channel);

		given(bootstrap.connect(any(SocketAddress.class))).willReturn(connectionChannelFuture);

		connector.doConnection(socketAddress, 1);

		swx.setConnectionState(ConnectionState.CONNECTED);

		// WHEN: doDisconnection
		connector.doDisconnection(1);

		// THEN: model is not updated
		runAndWait(() -> assertThat(swx.getConnectionState()).isEqualTo(ConnectionState.CONNECTED));

		// AND: has stop channelFuture
		verify(disconnectionChannelFuture).cancel();
	}

	@Test
	public void should_doDisconnection_fails_when_disconnection_fails() throws Exception {
		// GIVEN: model is connected
		ChannelFuture connectionChannelFuture = mock(ChannelFuture.class);

		ChannelFuture disconnectionChannelFuture = mock(ChannelFuture.class);
		given(disconnectionChannelFuture.isSuccess()).willReturn(false);

		Channel channel = mock(Channel.class);
		given(channel.close()).willReturn(disconnectionChannelFuture);
		given(connectionChannelFuture.getChannel()).willReturn(channel);

		given(bootstrap.connect(any(SocketAddress.class))).willReturn(connectionChannelFuture);

		connector.doConnection(socketAddress, 1);

		swx.setConnectionState(ConnectionState.CONNECTED);

		// WHEN: doDisconnection
		connector.doDisconnection(1);

		// THEN: model is not updated
		runAndWait(() -> assertThat(swx.getConnectionState()).isEqualTo(ConnectionState.CONNECTED));

		// WHEN: doDisconnection
		connector.doDisconnection(1);

		// THEN: model is not updated
		runAndWait(() -> assertThat(swx.getConnectionState()).isEqualTo(ConnectionState.CONNECTED));
	}

	@Test
	public void should_doDisconnection_success_when_channelFuture_success() throws Exception {
		// GIVEN: model is connected
		ChannelFuture connectionChannelFuture = mock(ChannelFuture.class);

		ChannelFuture disconnectionChannelFuture = mock(ChannelFuture.class);
		given(disconnectionChannelFuture.isSuccess()).willReturn(true);

		Channel channel = mock(Channel.class);
		given(channel.close()).willReturn(disconnectionChannelFuture);
		given(connectionChannelFuture.getChannel()).willReturn(channel);

		given(bootstrap.connect(any(SocketAddress.class))).willReturn(connectionChannelFuture);

		connector.doConnection(socketAddress, 1);

		swx.setConnectionState(ConnectionState.CONNECTED);

		// WHEN: doDisconnection
		connector.doDisconnection(1);

		// THEN: model is updated
		runAndWait(() -> assertThat(swx.getConnectionState()).isEqualTo(ConnectionState.DISCONNECTED));
	}

	@Test
	public void should_update_model_ConnectionState_in_JavaFX_thread_when_connection_is_up() throws InterruptedException, ExecutionException {
		// GIVEN: connection is DISCONNECTED
		swx.setConnectionState(ConnectionState.DISCONNECTED);

		// WHEN: Connection is up
		connector.channelConnected();

		// THEN: connection state has been updated in JavaFX thread
		runAndWait(() -> assertThat(swx.getConnectionState()).isEqualTo(ConnectionState.CONNECTED));
	}

	@Test
	public void should_update_model_ConnectionState_in_JavaFX_thread_when_connection_is_down() throws InterruptedException, ExecutionException {
		// GIVEN: connection is CONNECTED
		swx.setConnectionState(ConnectionState.CONNECTED);

		// WHEN: Connection is down
		connector.channelDisconnected();

		// THEN: connection state has been updated in JavaFX thread
		runAndWait(() -> assertThat(swx.getConnectionState()).isEqualTo(ConnectionState.DISCONNECTED));
	}

	@Test
	public void should_send_unsubscribe_msg_when_doDisconnection_success() {
		// GIVEN: model is connected
		ChannelFuture connectionChannelFuture = mock(ChannelFuture.class);

		ChannelFuture disconnectionChannelFuture = mock(ChannelFuture.class);
		given(disconnectionChannelFuture.isSuccess()).willReturn(true);

		Channel channel = mock(Channel.class);
		given(channel.close()).willReturn(disconnectionChannelFuture);
		given(connectionChannelFuture.getChannel()).willReturn(channel);

		given(bootstrap.connect(any(SocketAddress.class))).willReturn(connectionChannelFuture);

		connector.doConnection(socketAddress, 1);

		swx.setConnectionState(ConnectionState.CONNECTED);

		// WHEN: doDisconnection
		connector.doDisconnection(1);

		// AND: Should send unsubscribe message
		verify(sender).send(eq(channel), any(UnsubscribeCommand.class));
	}

	@Test
	public void should_update_InstrumentsModel_when_receive_onSnapshot_instruments() {
		// GIVEN: InstrumentsSnapshot with 2 instruments
		Instrument instrument1 = Instrument.newBuilder().setIsin("isin")
																		.setName("name")
																		.setDescription("description")
																		.setType(InstrumentType.STOCK)
																		.build();
		Instrument instrument2 = Instrument.newBuilder().setIsin("isin2")
																		.setName("name2")
																		.setDescription("description2")
																		.setType(InstrumentType.STOCK)
																		.build();
		InstrumentsSnapshot snapshot = InstrumentsSnapshot.newBuilder().addInstruments(instrument1).addInstruments(instrument2).build();

		// WHEN: connector receive snapshot
		connector.doOnSnapshot(snapshot);

		// THEN: update InstrumentsModel
		verify(instrumentsModel).add(instrument1, swx.getConnectionName());
		verify(instrumentsModel).add(instrument2, swx.getConnectionName());
	}

	@Test
	public void should_update_SummaryModel_when_receive_onSnapshot_summary() {
		// GIVEN: SummariesSnapshot
		Instrument instrument = Instrument.newBuilder().setIsin("isin")
																	  .setName("name")
																	  .setDescription("description")
																	  .setType(InstrumentType.STOCK)
																	  .build();
		Depth buyDepth = Depth.newBuilder().setDepth(0).setPrice(4.5).setQuantity(6).build();
		Depth sellDepth = Depth.newBuilder().setDepth(0).setPrice(7.8).setQuantity(9).build();
		Summary snapshot = Summary.newBuilder().setClose(1.2)
															.setInstrument(instrument)
															.setLast(2.3)
															.setMarketStatus(MarketStatus.OPEN)
															.setOpen(3.4)
															.setTimestamp(System.currentTimeMillis())
															.addBuyDepths(buyDepth)
															.addSellDepths(sellDepth)
															.build();

		SummariesSnapshot summaries = SummariesSnapshot.newBuilder().addSummaries(snapshot).build();
		
		// AND: model
		InstrumentModel instrumentModel = new InstrumentModel(instrument, swx.getConnectionName());
		given(instrumentsModel.getSummary(eq(instrument), eq(swx.getConnectionName()))).willReturn(instrumentModel.getSummary());
		
		// WHEN: connector receive snapshot
		connector.doOnSnapshot(summaries);

		// THEN: update InstrumentsModel
		com.kensai.gui.assertions.Assertions.assertThat(instrumentModel.getSummary()).isEqualTo(snapshot);
	}
}
