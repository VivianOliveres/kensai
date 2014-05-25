package com.kensai.gui.services.connectors;

public class PipelineFactoryService {

	public GuiChannelPipelineFactory createPipelineFactory(MarketConnector conector) {
		GuiChannelHandler handler = new GuiChannelHandler(conector);
		return new GuiChannelPipelineFactory(handler);
	}

}
