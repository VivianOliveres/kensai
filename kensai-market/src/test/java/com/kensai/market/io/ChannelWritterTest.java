package com.kensai.market.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.kensai.protocol.Trading.Messages;

@RunWith(MockitoJUnitRunner.class)
public class ChannelWritterTest {

	@Mock private Channel channel;

	private Messages msg = Messages.newBuilder().build();

	private ChannelWritter writter;

	@Before
	public void init() {
		given(channel.isOpen()).willReturn(true);
		given(channel.isConnected()).willReturn(true);
		given(channel.isWritable()).willReturn(true);

		writter = new ChannelWritter(channel);
	}

	@Test
	public void should_writter_delegate_to_channel() {
		// GIVEN: channel write with a future
		ChannelFuture channelFuture = mock(ChannelFuture.class);
		given(channel.write(any(Messages.class))).willReturn(channelFuture);

		// WHEN: write msg
		ChannelFuture future = writter.write(msg);

		// THEN: future is not null
		assertThat(future).isEqualTo(channelFuture);
		verify(channel).write(eq(msg));
	}

	@Test
	public void should_writter_return_null_when_channel_is_not_open() {
		// GIVEN: Channel is not open
		given(channel.isOpen()).willReturn(false);

		// WHEN: write msg
		ChannelFuture future = writter.write(msg);

		// THEN: future is null
		assertThat(future).isNull();
	}

	@Test
	public void should_writter_return_null_when_channel_is_not_connected() {
		// GIVEN: Channel is not connected
		given(channel.isConnected()).willReturn(false);

		// WHEN: write msg
		ChannelFuture future = writter.write(msg);

		// THEN: future is null
		assertThat(future).isNull();
	}

	@Test
	public void should_writter_return_null_when_channel_is_not_writable() {
		// GIVEN: Channel is not writable
		given(channel.isWritable()).willReturn(false);

		// WHEN: write msg
		ChannelFuture future = writter.write(msg);

		// THEN: future is null
		assertThat(future).isNull();
	}
}
