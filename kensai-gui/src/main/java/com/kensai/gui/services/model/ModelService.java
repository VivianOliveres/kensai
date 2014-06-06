package com.kensai.gui.services.model;

import com.kensai.gui.services.ApplicationContext;
import com.kensai.gui.services.connectors.MarketConnectorService;
import com.kensai.gui.services.model.instruments.InstrumentsModel;
import com.kensai.gui.services.model.market.MarketConnectionsModel;
import com.kensai.gui.services.model.orders.OrdersModel;

public class ModelService {

	private MarketConnectionsModel connections;
	private InstrumentsModel instruments;
	private MarketConnectorService connectorService;
	private OrdersModel orders;

	public ModelService(MarketConnectionsModel connexions) {
		this(connexions, null, new InstrumentsModel(), new OrdersModel());
	}

	public ModelService(MarketConnectionsModel connections, MarketConnectorService connectorService, InstrumentsModel instruments, OrdersModel orders) {
		this.connections = connections;
		this.connectorService = connectorService;
		this.instruments = instruments;
		this.orders = orders;
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

	public OrdersModel getOrders() {
		return orders;
	}
}
