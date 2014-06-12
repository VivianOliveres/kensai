package com.kensai.gui.services.connectors;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kensai.gui.services.ApplicationContext;
import com.kensai.gui.services.model.instruments.InstrumentModel;
import com.kensai.gui.services.model.instruments.InstrumentsModel;
import com.kensai.gui.services.model.instruments.SummaryModel;
import com.kensai.gui.services.model.market.MarketConnectionModel;
import com.kensai.gui.services.model.orders.OrdersModel;
import com.kensai.protocol.Trading.Execution;
import com.kensai.protocol.Trading.ExecutionsSnapshot;
import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.InstrumentsSnapshot;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.OrdersSnapshot;
import com.kensai.protocol.Trading.SubscribeCommand;
import com.kensai.protocol.Trading.SummariesSnapshot;
import com.kensai.protocol.Trading.Summary;
import com.kensai.protocol.Trading.UnsubscribeCommand;

public class MarketConnectorMessageHandler {
	private static final Logger log = LogManager.getLogger(MarketConnectorMessageHandler.class);

	private ApplicationContext context;
	private MarketConnectionModel model;

	private ConcurrentHashMap<Instrument, Summary> summariesUpdates = new ConcurrentHashMap<>();
	private ConcurrentLinkedQueue<Order> ordersUpdates = new ConcurrentLinkedQueue<>();

	public MarketConnectorMessageHandler(ApplicationContext context, MarketConnectionModel model) {
		this.context = context;
		this.model = model;

		context.getTaskService().getScheduledExecutor().scheduleAtFixedRate(() -> Platform.runLater(() -> doUpdateGui()), 750, 750, TimeUnit.MILLISECONDS);
	}

	protected Void doUpdateGui() {
		// Update each summary
		KeySetView<Instrument, Summary> keySet = summariesUpdates.keySet();
		log.debug("doUpdateGui for " + keySet.size() + " instruments");
		keySet.stream().map(instrument -> summariesUpdates.remove(instrument))
							.forEach(summary -> doUpdateSummay(summary));

		// Update each order
		log.info("doUpdateGui for " + ordersUpdates.size() + " orders");
		while (!ordersUpdates.isEmpty()) {
			Order order = ordersUpdates.remove();
			doUpdateOrder(order);
		}

		return null;
	}

	private Void doUpdateSummay(Summary summary) {
		SummaryModel summaryModel = context.getModelService().getInstruments().getSummary(summary.getInstrument(), model.getConnectionName());
		summaryModel.update(summary);
		return null;
	}

	private void doUpdateOrder(Order order) {
		InstrumentsModel instruments = context.getModelService().getInstruments();
		InstrumentModel instrument = instruments.getInstrument(order.getInstrument());

		OrdersModel orders = context.getModelService().getOrders();
		orders.add(order, instrument);
	}

	public void onSubscribe(SubscribeCommand subscribeCommand) {
		// TODO Auto-generated method stub
		log.info("onSubscribe [" + subscribeCommand.getStatus() + "]");
	}

	public void onUnsubscribe(UnsubscribeCommand unsubscribeCommand) {
		// TODO Auto-generated method stub
		log.info("onUnsubscribe [" + unsubscribeCommand.getStatus() + "]");
	}

	public void onSnapshot(SummariesSnapshot snapshot) {
		Platform.runLater(() -> doOnSnapshot(snapshot));
	}

	public void onSummary(Summary summary) {
		summariesUpdates.put(summary.getInstrument(), summary);
	}

	protected void doOnSnapshot(SummariesSnapshot snapshot) {
		snapshot.getSummariesList().forEach(summary -> onSummary(summary));
	}

	public void onSnapshot(ExecutionsSnapshot snapshot) {
		// TODO Auto-generated method stub
	}

	public void onSnapshot(OrdersSnapshot snapshot) {
		snapshot.getOrdersList().forEach(order -> onOrder(order));
	}

	public void onOrder(Order order) {
		ordersUpdates.add(order);
	}

	public void onSnapshot(InstrumentsSnapshot snapshot) {
		Platform.runLater(() -> doOnSnapshot(snapshot));
	}

	protected void doOnSnapshot(InstrumentsSnapshot snapshot) {
		InstrumentsModel instruments = context.getModelService().getInstruments();
		snapshot.getInstrumentsList().forEach(instrument -> instruments.add(instrument, model.getConnectionName()));
	}

	public void onExecution(Execution execution) {
		// TODO Auto-generated method stub
	}
}
