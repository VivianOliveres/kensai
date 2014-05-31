package com.kensai.gui.services.connectors;

import javafx.application.Platform;

import com.kensai.gui.services.ApplicationContext;
import com.kensai.gui.services.model.instruments.InstrumentsModel;
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

public class MarketConnectorMessageHandler {
	private ApplicationContext context;
	private MarketConnectionModel model;

	public MarketConnectorMessageHandler(ApplicationContext context, MarketConnectionModel model) {
		this.context = context;
		this.model = model;
	}

	public void onSubscribe(SubscribeCommand subscribeCommand) {
		// TODO Auto-generated method stub
	}

	public void onUnsubscribe(UnsubscribeCommand unsubscribeCommand) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
	}

}
