package com.kensai.gui.services;

import com.kensai.gui.views.instruments.InstrumentsViewController;
import com.kensai.gui.views.markets.MarketConnectionsViewController;

public class GuiService {

	private ApplicationContext context;

	private InstrumentsViewController instrumentsViewController;
	private MarketConnectionsViewController marketConnectionsViewController;

	public GuiService(ApplicationContext context) {
		this.context = context;
		initViews();
	}

	private void initViews() {
		instrumentsViewController = new InstrumentsViewController(context);
		marketConnectionsViewController = new MarketConnectionsViewController(context);
	}

	public MarketConnectionsViewController getMarketConnectionsViewController() {
		return marketConnectionsViewController;
	}

	public InstrumentsViewController getInstrumentsViewController() {
		return instrumentsViewController;
	}

}
