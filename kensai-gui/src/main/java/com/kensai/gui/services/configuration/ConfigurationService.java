package com.kensai.gui.services.configuration;

import com.kensai.gui.services.configuration.market.MarketConnectionConfigurationService;

public class ConfigurationService {

	private MarketConnectionConfigurationService marketConnexionConfigurationService;

	public ConfigurationService(MarketConnectionConfigurationService marketConnexionConfigurationService) {
		this.marketConnexionConfigurationService = marketConnexionConfigurationService;
	}

	public MarketConnectionConfigurationService getMarketConnexionConfigurationService() {
		return marketConnexionConfigurationService;
	}
}
