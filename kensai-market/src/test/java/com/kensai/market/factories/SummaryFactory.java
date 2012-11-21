package com.kensai.market.factories;

import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.MarketStatus;
import com.kensai.protocol.Trading.Summary;

public final class SummaryFactory {

	public static final double OPEN = 1.0;
	public static final double CLOSE = 1.0;
	public static final double LAST = 1.0;

	public static final MarketStatus STATUS = MarketStatus.OPEN;

	public static final Instrument INSTRUMENT = OrderFactory.INSTRUMENT;

	private SummaryFactory() {
		// Can not be instanciated
	}

	public static Summary.Builder create() {
		return Summary.newBuilder().setOpen(OPEN).setClose(CLOSE).setLast(LAST).setMarketStatus(STATUS).setInstrument(INSTRUMENT)
			.setTimestamp(System.currentTimeMillis());
	}

	public static Summary.Builder create(Instrument instr) {
		return create().setInstrument(INSTRUMENT);
	}
}
