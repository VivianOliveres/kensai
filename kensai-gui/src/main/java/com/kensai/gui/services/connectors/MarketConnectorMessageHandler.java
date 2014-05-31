package com.kensai.gui.services.connectors;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kensai.gui.services.ApplicationContext;
import com.kensai.gui.services.model.instruments.InstrumentsModel;
import com.kensai.gui.services.model.instruments.SummaryModel;
import com.kensai.gui.services.model.market.MarketConnectionModel;
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

	public MarketConnectorMessageHandler(ApplicationContext context, MarketConnectionModel model) {
		this.context = context;
		this.model = model;

		context.getTaskService().getScheduledExecutor().scheduleAtFixedRate(() -> Platform.runLater(() -> doUpdateGui()), 750, 750, TimeUnit.MILLISECONDS);
	}

	protected Void doUpdateGui() {
		// Update each summary
		KeySetView<Instrument, Summary> keySet = summariesUpdates.keySet();
		log.info("doUpdateGui for " + keySet.size());
		keySet.stream().map(instrument -> summariesUpdates.remove(instrument))
							.forEach(summary -> doUpdateSummay(summary));

		return null;
	}

	private Void doUpdateSummay(Summary summary) {
		SummaryModel summaryModel = context.getModelService().getInstruments().getSummary(summary.getInstrument(), model.getConnectionName());
		summaryModel.update(summary);
		return null;
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

	protected void doOnSnapshot(SummariesSnapshot snapshot) {
		InstrumentsModel instruments = context.getModelService().getInstruments();
		snapshot.getSummariesList().forEach(summary -> instruments.getSummary(summary.getInstrument(), model.getConnectionName()).update(summary));
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
		summariesUpdates.put(summary.getInstrument(), summary);
	}

}
