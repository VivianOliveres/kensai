package com.kensai.gui.services.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.kensai.gui.services.model.market.MarketConnexionModel;
import com.kensai.gui.xml.MarketConnexionDescriptor;
import com.kensai.gui.xml.MarketConnexionDescriptors;

public class ModelService {

	private final ObservableList<MarketConnexionModel> connexions = FXCollections.observableArrayList();

	public ModelService(MarketConnexionDescriptors connexions) {
		for (MarketConnexionDescriptor desc : connexions) {
			MarketConnexionModel connexion = new MarketConnexionModel(desc);
			this.connexions.add(connexion);
		}
	}

	public ObservableList<MarketConnexionModel> getConnexions() {
		return connexions;
	}

}
