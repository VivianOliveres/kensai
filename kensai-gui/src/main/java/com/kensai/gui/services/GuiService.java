package com.kensai.gui.services;

import com.kensai.gui.views.MarketConnectionsViewController;

public class GuiService {

	private ApplicationContext context;

	private MarketConnectionsViewController marketConnectionsViewController;

	public GuiService(ApplicationContext context) {
		this.context = context;
		initViews();
	}

	private void initViews() {
		marketConnectionsViewController = new MarketConnectionsViewController(context);
	}

	public MarketConnectionsViewController getMarketConnectionsViewController() {
		return marketConnectionsViewController;
	}

}
