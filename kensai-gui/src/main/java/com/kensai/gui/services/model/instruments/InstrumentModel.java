package com.kensai.gui.services.model.instruments;

import javafx.beans.property.SimpleStringProperty;

import com.google.common.base.Objects;
import com.kensai.protocol.Trading.Instrument;

public class InstrumentModel {

	private final SimpleStringProperty isin = new SimpleStringProperty();
	private final SimpleStringProperty name = new SimpleStringProperty();
	private final SimpleStringProperty market = new SimpleStringProperty();
	private final SimpleStringProperty description = new SimpleStringProperty();
	private final SimpleStringProperty type = new SimpleStringProperty();
	private final SimpleStringProperty connectionName = new SimpleStringProperty();

	private final SummaryModel summary;

	public InstrumentModel(Instrument copy, String marketConnectionName) {
		this(copy.getIsin(), copy.getName(), copy.getMarket(), copy.getDescription(), copy.getType().toString(), marketConnectionName);
	}

	public InstrumentModel(String isin, String name, String market, String description, String type, String marketConnectionName) {
		this.isin.set(isin);
		this.name.set(name);
		this.market.set(market);
		this.description.set(description);
		this.type.set(type);
		this.connectionName.set(marketConnectionName);

		summary = new SummaryModel(this);
	}

	public SimpleStringProperty isinProperty() {
		return isin;
	}

	public String getIsin() {
		return isin.get();
	}

	public void setIsin(String isin) {
		this.isin.set(isin);
	}

	public SimpleStringProperty nameProperty() {
		return name;
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public SimpleStringProperty marketProperty() {
		return market;
	}

	public String getMarket() {
		return market.get();
	}

	public void setMarket(String market) {
		this.market.set(market);
	}

	public SimpleStringProperty descriptionProperty() {
		return description;
	}

	public String getDescription() {
		return description.get();
	}

	public void setDescription(String description) {
		this.description.set(description);
	}

	public SimpleStringProperty typeProperty() {
		return type;
	}

	public String getType() {
		return type.get();
	}

	public void setType(String type) {
		this.type.set(type);
	}

	public SimpleStringProperty connectionNameProperty() {
		return connectionName;
	}

	public String getConnectionName() {
		return connectionName.get();
	}

	public void setConnectionName(String connectionName) {
		this.connectionName.set(connectionName);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(isin.get(), connectionName.get());
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof InstrumentModel) {
			InstrumentModel that = (InstrumentModel) object;
			return Objects.equal(this.isin.get(), that.isin.get()) && Objects.equal(this.connectionName.get(), that.connectionName.get());
		}

		return false;
	}

	public boolean equals(Instrument object) {
		return Objects.equal(this.isin.get(), object.getIsin()) 
			 && Objects.equal(this.name.get(), object.getName())
			 && Objects.equal(this.market.get(), object.getMarket())
			 && Objects.equal(this.description.get(), object.getDescription());
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("isin", isin.get())
			.add("name", name.get())
			.add("connectionName", connectionName.get())
			.toString();
	}

	public SummaryModel getSummary() {
		return summary;
	}

}
