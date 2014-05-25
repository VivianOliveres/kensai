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
import java.util.concurrent.ExecutionException;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.junit.Before;
import org.junit.Test;

import com.kensai.gui.AbstractTestJavaFX;
import com.kensai.gui.services.ApplicationContext;
import com.kensai.gui.services.model.market.ConnectionState;
import com.kensai.gui.services.model.market.MarketConnectionModel;
import com.kensai.gui.services.task.TaskService;

public class MarketConnectorTest extends AbstractTestJavaFX {

	private MarketConnectionModel swx = new MarketConnectionModel("SWX", "localhost", 1664, true);
	private InetSocketAddress socketAddress = new InetSocketAddress("localhost", 1664);

	private MarketConnector connector;

	private ClientBootstrap bootstrap = mock(ClientBootstrap.class);
	private TaskService taskService = mock(TaskService.class);

	@Before
	public void init() {
		ApplicationContext context = mock(ApplicationContext.class);
		given(context.getTaskService()).willReturn(taskService);

		connector = new MarketConnector(swx, context, bootstrap);
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
	public void should_doDisconnection_fails_when_cannot_retrieve_pipeline_from_bootstrap() throws Exception {
		// GIVEN: model is connected
		swx.setConnectionState(ConnectionState.CONNECTING);

		// AND: bootstrap has no pipeline
		ChannelPipelineFactory pipelineFactory = mock(ChannelPipelineFactory.class);
		given(pipelineFactory.getPipeline()).willThrow(Exception.class);
		given(bootstrap.getPipelineFactory()).willReturn(pipelineFactory);

		// WHEN: doDisconnection
		connector.doDisconnection(1);

		// THEN: model is not updated
		runAndWait(() -> assertThat(swx.getConnectionState()).isEqualTo(ConnectionState.CONNECTED));
	}

	@Test
	public void should_doDisconnection_fails_when_disconnection_fails_with_timeout() throws Exception {
		// GIVEN: model is connected
		swx.setConnectionState(ConnectionState.CONNECTING);

		// AND: ChannelFuture fails with timeout
		ChannelFuture channelFuture = mock(ChannelFuture.class);
		given(channelFuture.await(anyLong())).willThrow(InterruptedException.class);

		Channel channel = mock(Channel.class);
		given(channel.close()).willReturn(channelFuture);
		ChannelPipeline pipeline = mock(ChannelPipeline.class);
		given(pipeline.getChannel()).willReturn(channel);
		ChannelPipelineFactory pipelineFactory = mock(ChannelPipelineFactory.class);
		given(pipelineFactory.getPipeline()).willReturn(pipeline);
		given(bootstrap.getPipelineFactory()).willReturn(pipelineFactory);

		// WHEN: doDisconnection
		connector.doDisconnection(1);

		// THEN: model is not updated
		runAndWait(() -> assertThat(swx.getConnectionState()).isEqualTo(ConnectionState.CONNECTED));

		// AND: has stop channelFuture
		verify(channelFuture).cancel();
	}

	@Test
	public void should_doDisconnection_fails_when_disconnection_fails() throws Exception {
		// GIVEN: model is connected
		swx.setConnectionState(ConnectionState.CONNECTING);

		// AND: ChannelFuture fails
		ChannelFuture channelFuture = mock(ChannelFuture.class);
		given(channelFuture.isSuccess()).willReturn(false);

		Channel channel = mock(Channel.class);
		given(channel.close()).willReturn(channelFuture);
		ChannelPipeline pipeline = mock(ChannelPipeline.class);
		given(pipeline.getChannel()).willReturn(channel);
		ChannelPipelineFactory pipelineFactory = mock(ChannelPipelineFactory.class);
		given(pipelineFactory.getPipeline()).willReturn(pipeline);
		given(bootstrap.getPipelineFactory()).willReturn(pipelineFactory);

		// WHEN: doDisconnection
		connector.doDisconnection(1);

		// THEN: model is not updated
		runAndWait(() -> assertThat(swx.getConnectionState()).isEqualTo(ConnectionState.CONNECTED));
	}

	@Test
	public void should_doDisconnection_success_when_channelFuture_success() throws Exception {
		// GIVEN: model is connected
		swx.setConnectionState(ConnectionState.CONNECTING);

		// AND: ChannelFuture success
		ChannelFuture channelFuture = mock(ChannelFuture.class);
		given(channelFuture.isSuccess()).willReturn(true);

		Channel channel = mock(Channel.class);
		given(channel.close()).willReturn(channelFuture);
		ChannelPipeline pipeline = mock(ChannelPipeline.class);
		given(pipeline.getChannel()).willReturn(channel);
		ChannelPipelineFactory pipelineFactory = mock(ChannelPipelineFactory.class);
		given(pipelineFactory.getPipeline()).willReturn(pipeline);
		given(bootstrap.getPipelineFactory()).willReturn(pipelineFactory);

		// WHEN: doDisconnection
		connector.doDisconnection(1);

		// THEN: model is updated
		runAndWait(() -> assertThat(swx.getConnectionState()).isEqualTo(ConnectionState.DISCONNECTED));
	}
}
