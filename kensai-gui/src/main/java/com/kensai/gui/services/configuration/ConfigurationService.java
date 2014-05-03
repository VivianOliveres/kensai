package com.kensai.gui.services.configuration;

import com.kensai.gui.services.configuration.market.MarketConnexionConfigurationService;

public class ConfigurationService {

	private MarketConnexionConfigurationService marketConnexionConfigurationService;

	public ConfigurationService(MarketConnexionConfigurationService marketConnexionConfigurationService) {
		this.marketConnexionConfigurationService = marketConnexionConfigurationService;
	}

	public MarketConnexionConfigurationService getMarketConnexionConfigurationService() {
		return marketConnexionConfigurationService;
	}
}
