package com.kensai.gui.services.model.instruments;

import static org.assertj.core.api.Assertions.assertThat;
import javafx.collections.ObservableList;

import org.junit.Before;
import org.junit.Test;

import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.InstrumentType;

public class InstrumentsModelTest {

	private static final Instrument ACCOR = Instrument.newBuilder().setIsin("FR0000120404").setName("Accor").setDescription("description")
		.setType(InstrumentType.STOCK).setMarket("CAC").build();
	
	private static final Instrument VIVENDI = Instrument.newBuilder().setIsin("R0000127771").setName("Vivendi").setDescription("description")
		.setType(InstrumentType.STOCK).setMarket("CAC").build();

	private InstrumentsModel instruments;

	@Before
	public void init() {
		instruments = new InstrumentsModel();
	}

	@Test
	public void should_add_one_InstrumentModel() {
		// GIVEN: InstrumentModel
		InstrumentModel model = new InstrumentModel("isin", "name", "market", "description", "type", "marketConnectionName");

		// WHEN: add InstrumentModel
		instruments.add(model);

		// THEN: contains this InstrumentModel
		assertThat(instruments.getInstruments()).contains(model);
		assertThat(instruments.contains(model)).isTrue();
	}

	@Test
	public void should_not_add_same_InstrumentModel_twice() {
		// GIVEN: 2 InstrumentModel which are the same
		InstrumentModel model1 = new InstrumentModel("isin1", "name1", "market1", "description1", "type", "marketConnectionName1");
		InstrumentModel model2 = new InstrumentModel("isin1", "name1", "market1", "description1", "type", "marketConnectionName1");

		// WHEN: add these two instruments
		instruments.add(model1);
		instruments.add(model2);

		// THEN: contains only one instrument
		assertThat(instruments.getInstruments()).hasSize(1).containsOnly(model1).containsOnly(model2);

		assertThat(instruments.contains(model1)).isTrue();
		assertThat(instruments.contains(model2)).isTrue();
	}

	@Test
	public void should_add_one_Instrument() {
		// GIVEN: Instrument
		String marketConnectionName = "marketConnectionName";

		// WHEN: add Instrument
		instruments.add(ACCOR, marketConnectionName);

		// THEN: contains this Instrument
		ObservableList<InstrumentModel> list = instruments.getInstruments();
		assertThat(list).isNotNull().hasSize(1);
		assertThat(list.get(0)).isEqualTo(new InstrumentModel(ACCOR, marketConnectionName));

		assertThat(instruments.contains(ACCOR)).isTrue();
	}

	@Test
	public void should_not_add_same_Instrument_twice() {
		// GIVEN: 2 Instrument which are the same
		String marketConnectionName = "marketConnectionName";

		// WHEN: add these two instruments
		instruments.add(ACCOR, marketConnectionName);
		instruments.add(ACCOR, marketConnectionName);

		// THEN: contains only one instrument
		ObservableList<InstrumentModel> list = instruments.getInstruments();
		assertThat(list).isNotNull().hasSize(1);
		assertThat(list.get(0)).isEqualTo(new InstrumentModel(ACCOR, marketConnectionName));

		assertThat(instruments.contains(ACCOR)).isTrue();
	}

	@Test
	public void should_remove_InstrumentModel() {
		// GIVEN: contains 2 different InstrumentModel
		InstrumentModel model1 = new InstrumentModel("isin1", "name1", "market1", "description1", "type", "marketConnectionName1");
		instruments.add(model1);
		InstrumentModel model2 = new InstrumentModel("isin2", "name2", "market2", "description2", "type", "marketConnectionName2");
		instruments.add(model2);

		// WHEN: remove first InstrumentModel
		instruments.remove(model1);

		// THEN: Should contains only second InstrumentModel
		assertThat(instruments.getInstruments()).hasSize(1).containsOnly(model2);
	}
	
	@Test
	public void should_remove_Instrument() {
		// GIVEN: contains 2 different Instrument
		String marketConnectionName = "marketConnectionName";
		instruments.add(ACCOR, marketConnectionName);
		instruments.add(VIVENDI, marketConnectionName);

		// WHEN: remove first Instrument
		instruments.remove(ACCOR);

		// THEN: Should contains only second InstrumentModel
		assertThat(instruments).hasSize(1);
		assertThat(instruments.contains(VIVENDI)).isTrue();
	}

	@Test
	public void should_get_return_correct_SummaryModel() {
		// GIVEN: InstrumentModel in InstrumentsModel
		String marketConnectionName = "marketConnectionName";
		instruments.add(ACCOR, marketConnectionName);

		// WHEN: get
		SummaryModel summary = instruments.getSummary(ACCOR, marketConnectionName);

		// THEN: Return a valid summary
		assertThat(summary).isNotNull();
	}

	@Test
	public void should_get_return_correct_SummaryModel_and_create_instrument() {
		// GIVEN: InstrumentModel not in InstrumentsModel
		String marketConnectionName = "marketConnectionName";

		// AND: not in InstrumentsModel
		assertThat(instruments.contains(ACCOR)).isFalse();

		// WHEN: get
		SummaryModel summary = instruments.getSummary(ACCOR, marketConnectionName);

		// THEN: Return a valid summary
		assertThat(summary).isNotNull();

		// AND: instrument is in InstrumentsModel
		assertThat(instruments.contains(ACCOR)).isTrue();
	}

	@Test
	public void should_containsInstrument_is_ok() {
		// WHEN: add ACCOR instruments
		instruments.add(ACCOR, "market name");

		// THEN: Contains are ok
		assertThat(instruments).hasSize(1);
		assertThat(instruments.contains(ACCOR)).isTrue();
		assertThat(instruments.contains(VIVENDI)).isFalse();
	}
}
