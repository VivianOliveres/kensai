package com.kensai.gui.services.model;

import com.kensai.gui.services.ApplicationContext;
import com.kensai.gui.services.connectors.MarketConnectorService;
import com.kensai.gui.services.model.instruments.InstrumentsModel;
import com.kensai.gui.services.model.market.MarketConnectionsModel;

public class ModelService {

	private MarketConnectionsModel connections;
	private InstrumentsModel instruments;
	private MarketConnectorService connectorService;

	public ModelService(MarketConnectionsModel connexions) {
		this(connexions, null, new InstrumentsModel());
	}

	public ModelService(MarketConnectionsModel connections, MarketConnectorService connectorService, InstrumentsModel instruments) {
		this.connections = connections;
		this.connectorService = connectorService;
		this.instruments = instruments;
	}

	public void initMarketConnectorService(ApplicationContext context) {
		connectorService = new MarketConnectorService(connections, context);
	}

	public MarketConnectionsModel getConnexions() {
		return connections;
	}

	public MarketConnectorService getConnectorService() {
		return connectorService;
	}

	public InstrumentsModel getInstruments() {
		return instruments;
	}
}
