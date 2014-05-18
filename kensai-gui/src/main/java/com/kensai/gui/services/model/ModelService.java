package com.kensai.gui.services.model;

import com.kensai.gui.services.model.market.MarketConnectionsModel;

public class ModelService {

	private MarketConnectionsModel connexions;

	public ModelService(MarketConnectionsModel connexions) {
		this.connexions = connexions;
	}

	public MarketConnectionsModel getConnexions() {
		return connexions;
	}

}
