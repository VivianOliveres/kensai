package com.kensai.market.matchers;

import java.util.List;

import org.mockito.ArgumentMatcher;

import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.InstrumentsSnapshot;
import com.kensai.protocol.Trading.Messages;

public class InstrumentsSnapshotMatcher extends ArgumentMatcher {

	private List<Instrument> instruments;

	public InstrumentsSnapshotMatcher() {
		this(null);
	}

	public InstrumentsSnapshotMatcher(List<Instrument> instruments) {
		this.instruments = instruments;
	}

	@Override
	public boolean matches(Object argument) {
		if (argument instanceof InstrumentsSnapshot) {
			InstrumentsSnapshot cmd = (InstrumentsSnapshot) argument;
			return matchesInstruments(cmd);

		} else if (argument instanceof Messages) {
			Messages msg = (Messages) argument;
			return msg.hasInstrumentsSnapshot() && matchesInstruments(msg.getInstrumentsSnapshot());
		}

		return false;
	}

	private boolean matchesInstruments(InstrumentsSnapshot snapshot) {
		if (instruments == null) {
			return true;
		}

		return instruments.size() == snapshot.getInstrumentsCount();
	}

}
