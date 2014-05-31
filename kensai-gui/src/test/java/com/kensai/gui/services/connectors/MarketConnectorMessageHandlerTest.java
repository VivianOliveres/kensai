package com.kensai.gui.services.connectors;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.kensai.gui.services.ApplicationContext;
import com.kensai.gui.services.model.ModelService;
import com.kensai.gui.services.model.instruments.InstrumentModel;
import com.kensai.gui.services.model.instruments.InstrumentsModel;
import com.kensai.gui.services.model.market.MarketConnectionModel;
import com.kensai.gui.services.task.TaskService;
import com.kensai.protocol.Trading.Depth;
import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.InstrumentType;
import com.kensai.protocol.Trading.InstrumentsSnapshot;
import com.kensai.protocol.Trading.MarketStatus;
import com.kensai.protocol.Trading.SummariesSnapshot;
import com.kensai.protocol.Trading.Summary;

public class MarketConnectorMessageHandlerTest {
	
	private MarketConnectorMessageHandler handler;
	
	private MarketConnectionModel swx = new MarketConnectionModel("SWX", "localhost", 1664, false);

	private ApplicationContext context = mock(ApplicationContext.class);
	private TaskService taskService = mock(TaskService.class);
	private InstrumentsModel instrumentsModel = mock(InstrumentsModel.class);

	@Before
	public void init() {
		given(context.getTaskService()).willReturn(taskService);

		ModelService modelService = mock(ModelService.class);
		given(modelService.getInstruments()).willReturn(instrumentsModel);
		given(context.getModelService()).willReturn(modelService);

		handler = new MarketConnectorMessageHandler(context, swx);
	}

	@Test
	public void should_update_InstrumentsModel_when_receive_onSnapshot_instruments() {
		// GIVEN: InstrumentsSnapshot with 2 instruments
		Instrument instrument1 = Instrument.newBuilder().setIsin("isin")
																		.setName("name")
																		.setDescription("description")
																		.setType(InstrumentType.STOCK)
																		.build();
		Instrument instrument2 = Instrument.newBuilder().setIsin("isin2")
																		.setName("name2")
																		.setDescription("description2")
																		.setType(InstrumentType.STOCK)
																		.build();
		InstrumentsSnapshot snapshot = InstrumentsSnapshot.newBuilder().addInstruments(instrument1).addInstruments(instrument2).build();

		// WHEN: connector receive snapshot
		handler.doOnSnapshot(snapshot);

		// THEN: update InstrumentsModel
		verify(instrumentsModel).add(instrument1, swx.getConnectionName());
		verify(instrumentsModel).add(instrument2, swx.getConnectionName());
	}

	@Test
	public void should_update_SummaryModel_when_receive_onSnapshot_summary() {
		// GIVEN: SummariesSnapshot
		Instrument instrument = Instrument.newBuilder().setIsin("isin")
																	  .setName("name")
																	  .setDescription("description")
																	  .setType(InstrumentType.STOCK)
																	  .build();
		Depth buyDepth = Depth.newBuilder().setDepth(0).setPrice(4.5).setQuantity(6).build();
		Depth sellDepth = Depth.newBuilder().setDepth(0).setPrice(7.8).setQuantity(9).build();
		Summary snapshot = Summary.newBuilder().setClose(1.2)
															.setInstrument(instrument)
															.setLast(2.3)
															.setMarketStatus(MarketStatus.OPEN)
															.setOpen(3.4)
															.setTimestamp(System.currentTimeMillis())
															.addBuyDepths(buyDepth)
															.addSellDepths(sellDepth)
															.build();

		SummariesSnapshot summaries = SummariesSnapshot.newBuilder().addSummaries(snapshot).build();

		// AND: model
		InstrumentModel instrumentModel = new InstrumentModel(instrument, swx.getConnectionName());
		given(instrumentsModel.getSummary(eq(instrument), eq(swx.getConnectionName()))).willReturn(instrumentModel.getSummary());

		// WHEN: connector receive snapshot
		handler.doOnSnapshot(summaries);

		// THEN: update InstrumentsModel
		com.kensai.gui.assertions.Assertions.assertThat(instrumentModel.getSummary()).isEqualTo(snapshot);
	}
}
