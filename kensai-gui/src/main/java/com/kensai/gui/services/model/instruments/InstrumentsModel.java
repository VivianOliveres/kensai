package com.kensai.gui.services.model.instruments;

import java.util.Iterator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.kensai.protocol.Trading.Instrument;

public class InstrumentsModel implements Iterable<InstrumentModel> {
	private final ObservableList<InstrumentModel> instruments = FXCollections.observableArrayList();

	public void add(InstrumentModel instrument) {
		if (!instruments.contains(instrument)) {
			instruments.add(instrument);
		}
	}

	public void add(Instrument instrument, String marketConnectionName) {
		if (!contains(instrument)) {
			instruments.add(new InstrumentModel(instrument, marketConnectionName));
		}
	}

	public boolean contains(InstrumentModel instrument) {
		return instruments.contains(instrument);
	}

	public boolean contains(Instrument instrument) {
		return !instruments.filtered(model -> model.equals(instrument)).isEmpty();
	}

	public void remove(InstrumentModel instrument) {
		instruments.remove(instrument);
	}

	public void remove(Instrument instrument) {
		if (contains(instrument)) {
			InstrumentModel instrumentModel = instruments.filtered(model -> model.equals(instrument)).get(0);
			instruments.remove(instrumentModel);
		}
	}

	public ObservableList<InstrumentModel> getInstruments() {
		return instruments;
	}

	@Override
	public Iterator<InstrumentModel> iterator() {
		return instruments.iterator();
	}

	public InstrumentModel getInstrument(Instrument instrument) {
		return instruments.stream().filter(model -> model.equals(instrument)).findFirst().get();
	}

	public SummaryModel getSummary(Instrument instrument, String marketConnectionName) {
		InstrumentModel instrumentModel;
		if (contains(instrument)) {
			instrumentModel = instruments.filtered(model -> model.equals(instrument)).get(0);

		} else {
			instrumentModel = new InstrumentModel(instrument, marketConnectionName);
			instruments.add(instrumentModel);
		}

		return instrumentModel.getSummary();
	}

}
