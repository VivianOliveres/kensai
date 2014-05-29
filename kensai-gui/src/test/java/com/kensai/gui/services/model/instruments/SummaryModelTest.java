package com.kensai.gui.services.model.instruments;

import org.junit.Before;
import org.junit.Test;

import com.kensai.gui.assertions.Assertions;
import com.kensai.protocol.Trading.Depth;
import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.InstrumentType;
import com.kensai.protocol.Trading.MarketStatus;
import com.kensai.protocol.Trading.Summary;

public class SummaryModelTest {

	private InstrumentModel instrument = new InstrumentModel("FR0000120404", "Accor", "CAC", "description", "STOCK", "CAC");
	private SummaryModel summary;

	@Before
	public void init() {
		summary = instrument.getSummary();

		Assertions.assertThat(summary).hasBuyPrice(0)
												.hasBuyQty(0)
												.hasClose(0)
												.hasInstrument(instrument)
												.hasLast(0)
												.hasMarketStatus(MarketStatus.CLOSE)
												.hasOpen(0)
												.hasSellPrice(0)
												.hasSellQty(0)
												.hasTimestamp(0);
	}

	@Test
	public void should_update_summary() {
		// GIVEN: Summary snapshot
		Instrument instrumentSnapshot = Instrument.newBuilder()
																.setDescription(instrument.getDescription())
																.setIsin(instrument.getIsin())
																.setMarket(instrument.getMarket())
																.setName(instrument.getName())
																.setType(InstrumentType.valueOf(instrument.getType()))
																.build();
		double close = 1.2;
		double last = 2.3;
		double open = 3.4;
		long timestamp = System.currentTimeMillis();
		Depth buyDepth = Depth.newBuilder().setDepth(0).setPrice(4.5).setQuantity(6).build();
		Depth sellDepth = Depth.newBuilder().setDepth(0).setPrice(7.8).setQuantity(9).build();
		MarketStatus status = MarketStatus.OPEN;
		Summary snapshot = Summary.newBuilder().setClose(close)
															.setInstrument(instrumentSnapshot)
															.setLast(last)
															.setMarketStatus(status)
															.setOpen(open)
															.setTimestamp(timestamp)
															.addBuyDepths(buyDepth)
															.addSellDepths(sellDepth)
															.build();

		// WHEN: update summary
		summary.update(snapshot);

		// THEN: summary is updated
		Assertions.assertThat(summary).hasInstrument(instrument).isEqualTo(snapshot);
	}
}
