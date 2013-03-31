package com.kensai.market;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.assertj.core.api.Assertions;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.kensai.protocol.Trading.BuySell;
import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.InstrumentType;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.OrderAction;

public class MarketClient {

	private final String host;
	private final int port;

	public MarketClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	private void run() {
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
			Executors.newCachedThreadPool()));

		// Configure the event pipeline factory.
		bootstrap.setPipelineFactory(new MarketClientChannelPipelineFactory());

		// Make a new connection.
		ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(host, port));

		// Wait until the connection is made successfully.
		Channel channel = connectFuture.awaitUninterruptibly().getChannel();

		// Get the handler instance to initiate the request.
		MarketClientChannelHandler handler = channel.getPipeline().get(MarketClientChannelHandler.class);

		try {
			runScenarii(handler);
		} catch (InterruptedException e) {
			Assertions.fail("Error during scenarii", e);

		} finally {
			// Close the connection.
			channel.close().awaitUninterruptibly();

			// Shut down all thread pools to exit.
			bootstrap.releaseExternalResources();
		}
	}

	private void runScenarii(MarketClientChannelHandler handler) throws InterruptedException {
		// Subscribe
		handler.sendSubscribe();
		Thread.sleep(1000);
		assertThat(handler.hasReceiveSubscribeAnswer()).isTrue();
		assertThat(handler.isSubscribeOk()).isTrue();
		assertThat(handler.getExecutionsSnapshot()).isNotNull();
		assertThat(handler.getOrdersSnapshot()).isNotNull();
		assertThat(handler.getSummariesSnapshot()).isNotNull();
		assertThat(handler.getInstrumentsSnapshot()).isNotNull();

		// Send order
		Instrument instrument = Instrument.newBuilder().setIsin("FR").setMarket("Euronext").setType(InstrumentType.STOCK).build();
		Order order = Order.newBuilder().setInstrument(instrument).setAction(OrderAction.INSERT).setInitialQuantity(123).setPrice(456.789)
			.setSide(BuySell.BUY).setUser("client-user").setUserData("user-data").build();
		handler.sendOrder(order);
		Thread.sleep(1000);
		assertThat(handler.hasReceivedOrder()).isTrue();
		assertThat(handler.isOrderOk()).isTrue();

		// Unsubscribe
		handler.sendUnsubscribe();
		Thread.sleep(1000);
		assertThat(handler.hasReceiveUnsubscribeAnswer()).isTrue();
		assertThat(handler.isUnsubscribeOk()).isTrue();
	}

	public static void main(String[] args) {
		String host = "localhost";
		int port = 1664;
		new MarketClient(host, port).run();
	}
}
