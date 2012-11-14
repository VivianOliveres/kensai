package com.kensai.market;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Set;

import com.kensai.protocol.Trading.Instrument;

public class InstrumentDepth {

	private final Instrument instrument;

	private Set<DepthRow> buyDepths = newHashSet();
	private Set<DepthRow> sellDepths = newHashSet();

	public InstrumentDepth(Instrument instrument) {
		this.instrument = instrument;
	}

	public Instrument getInstrument() {
		return instrument;
	}

}
