package com.kensai.gui.services.model.instruments;

import static org.assertj.core.api.Assertions.assertThat;
import javafx.collections.ObservableList;

import org.junit.Before;
import org.junit.Test;

import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.InstrumentType;

public class InstrumentsModelTest {

	private InstrumentsModel instruments;

	@Before
	public void init() {
		instruments = new InstrumentsModel();
	}

	@Test
	public void should_add_one_InstrumentModel() {
		// GIVEN: InstrumentModel
		InstrumentModel instrument = new InstrumentModel("isin", "name", "market", "description", "type", "marketConnectionName");

		// WHEN: add InstrumentModel
		instruments.add(instrument);

		// THEN: contains this InstrumentModel
		assertThat(instruments.getInstruments()).contains(instrument);
		assertThat(instruments.contains(instrument)).isTrue();
	}

	@Test
	public void should_not_add_same_InstrumentModel_twice() {
		// GIVEN: 2 InstrumentModel which are the same
		InstrumentModel instrument1 = new InstrumentModel("isin1", "name1", "market1", "description1", "type", "marketConnectionName1");
		InstrumentModel instrument2 = new InstrumentModel("isin1", "name1", "market1", "description1", "type", "marketConnectionName1");

		// WHEN: add these two instruments
		instruments.add(instrument1);
		instruments.add(instrument2);

		// THEN: contains only one instrument
		assertThat(instruments.getInstruments()).hasSize(1).containsOnly(instrument1).containsOnly(instrument2);

		assertThat(instruments.contains(instrument1)).isTrue();
		assertThat(instruments.contains(instrument2)).isTrue();
	}

	@Test
	public void should_add_one_Instrument() {
		// GIVEN: Instrument
		String marketConnectionName = "marketConnectionName";
		Instrument instrument = Instrument.newBuilder().setIsin("isin").setName("name").setDescription("description").setType(InstrumentType.STOCK)
			.build();

		// WHEN: add Instrument
		instruments.add(instrument, marketConnectionName);

		// THEN: contains this Instrument
		ObservableList<InstrumentModel> list = instruments.getInstruments();
		assertThat(list).isNotNull().hasSize(1);
		assertThat(list.get(0)).isEqualTo(new InstrumentModel(instrument, marketConnectionName));

		assertThat(instruments.contains(instrument)).isTrue();
	}

	@Test
	public void should_not_add_same_Instrument_twice() {
		// GIVEN: 2 Instrument which are the same
		String marketConnectionName = "marketConnectionName";
		Instrument instrument1 = Instrument.newBuilder().setIsin("isin")
																		.setName("name")
																		.setDescription("description")
																		.setType(InstrumentType.STOCK)
																		.build();
		Instrument instrument2 = Instrument.newBuilder().setIsin("isin")
																		.setName("name")
																		.setDescription("description")
																		.setType(InstrumentType.STOCK)
																		.build();

		// WHEN: add these two instruments
		instruments.add(instrument1, marketConnectionName);
		instruments.add(instrument2, marketConnectionName);

		// THEN: contains only one instrument
		ObservableList<InstrumentModel> list = instruments.getInstruments();
		assertThat(list).isNotNull().hasSize(1);
		assertThat(list.get(0)).isEqualTo(new InstrumentModel(instrument1, marketConnectionName))
									  .isEqualTo(new InstrumentModel(instrument2, marketConnectionName));

		assertThat(instruments.contains(instrument1)).isTrue();
		assertThat(instruments.contains(instrument2)).isTrue();
	}

	@Test
	public void should_remove_InstrumentModel() {
		// GIVEN: contains 2 different InstrumentModel
		InstrumentModel instrument1 = new InstrumentModel("isin1", "name1", "market1", "description1", "type", "marketConnectionName1");
		instruments.add(instrument1);
		InstrumentModel instrument2 = new InstrumentModel("isin2", "name2", "market2", "description2", "type", "marketConnectionName2");
		instruments.add(instrument2);

		// WHEN: remove first InstrumentModel
		instruments.remove(instrument1);

		// THEN: Should contains only second InstrumentModel
		assertThat(instruments.getInstruments()).hasSize(1).containsOnly(instrument2);
	}
	
	@Test
	public void should_remove_Instrument() {
		// GIVEN: contains 2 different Instrument
		String marketConnectionName = "marketConnectionName";
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
		instruments.add(instrument1, marketConnectionName);
		instruments.add(instrument2, marketConnectionName);

		// WHEN: remove first Instrument
		instruments.remove(instrument1);

		// THEN: Should contains only second InstrumentModel
		assertThat(instruments.getInstruments()).hasSize(1);
	}

	@Test
	public void should_get_return_correct_SummaryModel() {
		// GIVEN: InstrumentModel in InstrumentsModel
		String marketConnectionName = "marketConnectionName";
		Instrument instrument1 = Instrument.newBuilder().setIsin("isin")
																		.setName("name")
																		.setDescription("description")
																		.setType(InstrumentType.STOCK)
																		.build();
		instruments.add(instrument1, marketConnectionName);

		// WHEN: get
		SummaryModel summary = instruments.getSummary(instrument1, marketConnectionName);

		// THEN: Return a valid summary
		assertThat(summary).isNotNull();
	}
	

	@Test
	public void should_get_return_correct_SummaryModel_and_create_instrument() {
		// GIVEN: InstrumentModel not in InstrumentsModel
		String marketConnectionName = "marketConnectionName";
		Instrument instrument1 = Instrument.newBuilder().setIsin("isin")
																		.setName("name")
																		.setDescription("description")
																		.setType(InstrumentType.STOCK)
																		.build();
		// AND: not in InstrumentsModel
		assertThat(instruments.contains(instrument1)).isFalse();

		// WHEN: get
		SummaryModel summary = instruments.getSummary(instrument1, marketConnectionName);

		// THEN: Return a valid summary
		assertThat(summary).isNotNull();

		// AND: instrument is in InstrumentsModel
		assertThat(instruments.contains(instrument1)).isTrue();
	}
}
