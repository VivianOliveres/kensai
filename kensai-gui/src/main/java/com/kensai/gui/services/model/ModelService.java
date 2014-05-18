package com.kensai.gui.services.model;

import com.kensai.gui.services.model.market.MarketConnexionsModel;

public class ModelService {

	private MarketConnexionsModel connexions;

	public ModelService(MarketConnexionsModel connexions) {
		this.connexions = connexions;
	}

	public MarketConnexionsModel getConnexions() {
		return connexions;
	}

}
