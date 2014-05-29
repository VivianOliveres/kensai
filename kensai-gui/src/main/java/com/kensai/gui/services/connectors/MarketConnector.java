package com.kensai.gui.services.connectors;

import java.net.InetSocketAddress;

import javafx.application.Platform;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import com.google.common.base.Objects;
import com.kensai.gui.services.ApplicationContext;
import com.kensai.gui.services.model.instruments.InstrumentsModel;
import com.kensai.gui.services.model.market.ConnectionState;
import com.kensai.gui.services.model.market.MarketConnectionModel;
import com.kensai.protocol.Trading.Execution;
import com.kensai.protocol.Trading.ExecutionsSnapshot;
import com.kensai.protocol.Trading.InstrumentsSnapshot;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.OrdersSnapshot;
import com.kensai.protocol.Trading.SubscribeCommand;
import com.kensai.protocol.Trading.SummariesSnapshot;
import com.kensai.protocol.Trading.Summary;
import com.kensai.protocol.Trading.UnsubscribeCommand;

public class MarketConnector {
	private static final Logger log = LogManager.getLogger(MarketConnector.class);

	public static final String DEFAULT_USER = "GUI_USER";

	private ApplicationContext context;
	private MarketConnectionModel model;

	private MarketConnectorMessageSender sender;

	private ClientBootstrap bootstrap;
	private Channel connectedChannel;

	public MarketConnector(MarketConnectionModel model, ApplicationContext context) {
		this.model = model;
		this.context = context;
		this.bootstrap = ClientBoostrapFactory.create(context, this);
		this.sender = new MarketConnectorMessageSender();
	}

	public MarketConnector(MarketConnectionModel model, ApplicationContext context, ClientBootstrap bootstrap, MarketConnectorMessageSender sender) {
		this.model = model;
		this.context = context;
		this.bootstrap = bootstrap;
		this.sender = sender;
	}

	public void connect() {
		if (model.getConnectionState() != ConnectionState.DISCONNECTED) {
			log.warn("Can not connect to [" + getMarketName() + "] which is not DISCONNECTED: current state [" + model.getConnectionState() + "]");
			return;
		}

		// Update GUI
		model.setConnectionState(ConnectionState.CONNECTING);

		// Connect in background
		final InetSocketAddress socketAddress = new InetSocketAddress(model.getHost(), model.getPort());
		context.getTaskService().runInBackground(() -> doConnection(socketAddress, 5000));
	}

	protected Void doConnection(InetSocketAddress socketAddress, long timeout) {
		log.info("Connecting to [" + getMarketName() + "] ...");

		// Connect
		ChannelFuture connectFuture = bootstrap.connect(socketAddress);
		connectedChannel = connectFuture.getChannel();

		// Wait until the connection is made successfully.
		try {
			connectFuture.await(timeout);
		} catch (InterruptedException e) {
			log.error("Timeout when connecting to " + getMarketName(), e);
			Platform.runLater(() -> model.setConnectionState(ConnectionState.DISCONNECTED));
			connectFuture.cancel();
			return null;
		}

		if (connectFuture.isSuccess()) {
			log.info("Connected to " + getMarketName());
			Platform.runLater(() -> model.setConnectionState(ConnectionState.CONNECTED));

			// send subscribe to market
			SubscribeCommand cmd = SubscribeCommand.newBuilder().setUser(DEFAULT_USER).build();
			sender.send(connectedChannel, cmd);

		} else {
			log.error("Can not connect to " + getMarketName(), connectFuture.getCause());
			Platform.runLater(() -> model.setConnectionState(ConnectionState.DISCONNECTED));
		}

		return null;
	}

	public void disconnect() {
		// Only disconnect if model is !DISCONNECTED
		if (model.getConnectionState() == ConnectionState.DISCONNECTED) {
			log.warn("Disconnection fails - reason: [" + getMarketName() + "] is already disconnected");
			return;
		}

		// Update GUI
		model.setConnectionState(ConnectionState.CONNECTING);

		// Disconnect in background
		context.getTaskService().runInBackground(() -> doDisconnection(5000));
	}

	protected Void doDisconnection(long timeout) {
		log.info("Diconnecting to [" + getMarketName() + "] ...");
		// Send unsubscribe
		UnsubscribeCommand cmd = UnsubscribeCommand.newBuilder().setUser(DEFAULT_USER).build();
		sender.send(connectedChannel, cmd);

		// Close channel
		ChannelFuture closeConnectionFuture = connectedChannel.close();

		try {
			closeConnectionFuture.await(timeout);
		} catch (InterruptedException e) {
			log.error("Timeout when disconnecting to [" + getMarketName() + "]", e);
			Platform.runLater(() -> model.setConnectionState(ConnectionState.CONNECTED));
			closeConnectionFuture.cancel();
			return null;
		}

		if (closeConnectionFuture.isSuccess()) {
			log.info("Disconnected to " + getMarketName());
			Platform.runLater(() -> model.setConnectionState(ConnectionState.DISCONNECTED));

		} else {
			log.error("Can not disconnect to " + getMarketName(), closeConnectionFuture.getCause());
			Platform.runLater(() -> model.setConnectionState(ConnectionState.CONNECTED));
		}

		return null;
	}

	public ConnectionState getConnectionState() {
		return model.getConnectionState();
	}

	public MarketConnectionModel getModel() {
		return model;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(model);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof MarketConnector) {
			MarketConnector that = (MarketConnector) object;
			return Objects.equal(this.model, that.model);
		}

		return false;
	}

	public String getMarketName() {
		return model.getConnectionName();
	}

	public void channelConnected() {
		Platform.runLater(() -> model.setConnectionState(ConnectionState.CONNECTED));
	}

	public void channelDisconnected() {
		Platform.runLater(() -> model.setConnectionState(ConnectionState.DISCONNECTED));
	}

	public void onSubscribe(SubscribeCommand subscribeCommand) {
		// TODO Auto-generated method stub
	}

	public void onUnsubscribe(UnsubscribeCommand unsubscribeCommand) {
		// TODO Auto-generated method stub
	}

	public void onSnapshot(SummariesSnapshot snapshot) {
		// TODO Auto-generated method stub
	}

	public void onSnapshot(ExecutionsSnapshot snapshot) {
		// TODO Auto-generated method stub
	}

	public void onSnapshot(OrdersSnapshot snapshot) {
		// TODO Auto-generated method stub
	}

	public void onSnapshot(InstrumentsSnapshot snapshot) {
		Platform.runLater(() -> doOnSnapshot(snapshot));
	}

	protected void doOnSnapshot(InstrumentsSnapshot snapshot) {
		InstrumentsModel instruments = context.getModelService().getInstruments();
		snapshot.getInstrumentsList().forEach(instrument -> instruments.add(instrument, model.getConnectionName()));
	}

	public void onOrder(Order order) {
		// TODO Auto-generated method stub
	}

	public void onExecution(Execution execution) {
		// TODO Auto-generated method stub
	}

	public void onSummary(Summary summary) {
		// TODO Auto-generated method stub
	}

}
