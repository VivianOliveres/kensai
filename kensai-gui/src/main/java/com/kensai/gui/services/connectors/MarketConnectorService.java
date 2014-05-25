package com.kensai.gui.services.connectors;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

import org.reactfx.EventStreams;

import com.google.common.collect.Lists;
import com.kensai.gui.services.ApplicationContext;
import com.kensai.gui.services.model.market.MarketConnectionModel;
import com.kensai.gui.services.model.market.MarketConnectionsModel;

public class MarketConnectorService {

	private List<MarketConnector> connectors = new ArrayList<>();
	private ApplicationContext context;

	public MarketConnectorService(MarketConnectionsModel model, ApplicationContext context) {
		this(model.getConnexions(), context);
	}

	public MarketConnectorService(ObservableList<MarketConnectionModel> connections, ApplicationContext context) {
		this.context = context;

		// Init
		connections.forEach(model -> connectors.add(new MarketConnector(model, context)));

		// Register to changes on model
		EventStreams.changesOf(connections).subscribe(change -> doUpdateConnectors(change));
	}

	private Void doUpdateConnectors(Change<? extends MarketConnectionModel> change) {
		while (change.next()) {
			if (change.wasPermutated() || change.wasUpdated()) {
				continue;

			} else {
				change.getRemoved().forEach(model -> connectors.remove(new MarketConnector(model, context)));
				change.getAddedSubList().forEach(model -> connectors.add(new MarketConnector(model, context)));
			}
		}

		return null;
	}

	public List<MarketConnector> getConnectors() {
		List<MarketConnector> copy = Lists.newArrayList(connectors);
		return copy;
	}
}
