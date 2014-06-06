package com.kensai.gui.services;

import com.kensai.gui.views.instruments.InstrumentsViewController;
import com.kensai.gui.views.markets.MarketConnectionsViewController;
import com.kensai.gui.views.orders.OrdersViewController;

public class GuiService {

	private ApplicationContext context;

	private InstrumentsViewController instrumentsViewController;
	private MarketConnectionsViewController marketConnectionsViewController;
	private OrdersViewController ordersViewController;

	public GuiService(ApplicationContext context) {
		this.context = context;
		initViews();
	}

	private void initViews() {
		instrumentsViewController = new InstrumentsViewController(context);
		marketConnectionsViewController = new MarketConnectionsViewController(context);
		ordersViewController = new OrdersViewController(context);
	}

	public MarketConnectionsViewController getMarketConnectionsViewController() {
		return marketConnectionsViewController;
	}

	public InstrumentsViewController getInstrumentsViewController() {
		return instrumentsViewController;
	}

	public OrdersViewController getOrdersViewController() {
		return ordersViewController;
	}

}
