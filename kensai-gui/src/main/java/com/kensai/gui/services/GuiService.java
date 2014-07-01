package com.kensai.gui.services;

import com.kensai.gui.views.markets.MarketConnectionsViewController;
import com.kensai.gui.views.orders.OrdersViewController;
import com.kensai.gui.views.summary.SummaryViewController;

public class GuiService {

	private ApplicationContext context;

	private SummaryViewController instrumentsViewController;
	private MarketConnectionsViewController marketConnectionsViewController;
	private OrdersViewController ordersViewController;

	public GuiService(ApplicationContext context) {
		this.context = context;
		initViews();
	}

	private void initViews() {
		instrumentsViewController = new SummaryViewController(context);
		marketConnectionsViewController = new MarketConnectionsViewController(context);
		ordersViewController = new OrdersViewController(context);
	}

	public MarketConnectionsViewController getMarketConnectionsViewController() {
		return marketConnectionsViewController;
	}

	public SummaryViewController getInstrumentsViewController() {
		return instrumentsViewController;
	}

	public OrdersViewController getOrdersViewController() {
		return ordersViewController;
	}

}
