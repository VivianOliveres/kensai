package com.kensai.gui.services.model.market;

import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.reactfx.EventStreams;

import com.kensai.gui.services.configuration.market.MarketConnexionConfigurationService;
import com.kensai.gui.xml.MarketConnexionDescriptors;

public class MarketConnexionsModel {

	private final ObservableList<MarketConnexionModel> connexions = FXCollections.observableArrayList();

	public MarketConnexionsModel(MarketConnexionConfigurationService service) {
		// Init ObservableList
		service.getConnexions().forEach(desc -> connexions.add(new MarketConnexionModel(desc)));

		// Listen to changes on ObservableLIst to report them in service
		EventStreams.changesOf(connexions)
						.map(change -> connexions.stream()
														 .map(model -> model.toDescriptor())
														 .collect(Collectors.toList()))
						.subscribe(descriptors -> service.setConnexions(new MarketConnexionDescriptors(descriptors)));	
	}

	public ObservableList<MarketConnexionModel> getConnexions() {
		return connexions;
	}

}
