package com.kensai.gui.services.model.market;

import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.reactfx.EventStreams;

import com.kensai.gui.services.configuration.market.MarketConnectionConfigurationService;
import com.kensai.gui.xml.MarketConnexionDescriptors;

public class MarketConnectionsModel {

	private final ObservableList<MarketConnectionModel> connections = FXCollections.observableArrayList();

	public MarketConnectionsModel(MarketConnectionConfigurationService service) {
		// Init ObservableList
		service.getConnexions().forEach(desc -> connections.add(new MarketConnectionModel(desc)));

		// Listen to changes on ObservableLIst to report them in service
		EventStreams.changesOf(connections)
						.map(change -> connections.stream()
														 .map(model -> model.toDescriptor())
														 .collect(Collectors.toList()))
						.subscribe(descriptors -> service.setConnexions(new MarketConnexionDescriptors(descriptors)));	
	}

	public ObservableList<MarketConnectionModel> getConnexions() {
		return connections;
	}

}
