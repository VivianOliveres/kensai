package com.kensai.gui.services.model.instruments;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Objects;
import com.kensai.protocol.Trading;
import com.kensai.protocol.Trading.Depth;
import com.kensai.protocol.Trading.MarketStatus;
import com.kensai.protocol.Trading.Summary;

public class SummaryModel {
	private static final Logger log = LogManager.getLogger(SummaryModel.class);

	private final InstrumentModel instrument;

	private final SimpleObjectProperty<MarketStatus> marketStatus = new SimpleObjectProperty<Trading.MarketStatus>(MarketStatus.CLOSE);
	private final SimpleDoubleProperty open = new SimpleDoubleProperty();
	private final SimpleDoubleProperty last = new SimpleDoubleProperty();
	private final SimpleDoubleProperty close = new SimpleDoubleProperty();
	private final SimpleLongProperty timestamp = new SimpleLongProperty();

	private final SimpleLongProperty buyQty = new SimpleLongProperty();
	private final SimpleDoubleProperty buyPrice = new SimpleDoubleProperty();

	private final SimpleLongProperty sellQty = new SimpleLongProperty();
	private final SimpleDoubleProperty sellPrice = new SimpleDoubleProperty();

	public SummaryModel(InstrumentModel instrumentModel) {
		this.instrument = instrumentModel;
	}

	public SimpleObjectProperty<MarketStatus> marketStatusProperty() {
		return marketStatus;
	}

	public MarketStatus getMarketStatus() {
		return marketStatus.get();
	}

	public void setMarketStatus(MarketStatus marketStatus) {
		this.marketStatus.set(marketStatus);
	}

	public SimpleDoubleProperty openProperty() {
		return open;
	}

	public double getOpen() {
		return open.get();
	}

	public void setOpen(double open) {
		this.open.set(open);
	}

	public SimpleDoubleProperty lastProperty() {
		return last;
	}

	public double getLast() {
		return last.get();
	}

	public void setLast(double last) {
		this.last.set(last);
	}

	public SimpleDoubleProperty closeProperty() {
		return close;
	}

	public double getClose() {
		return close.get();
	}

	public void setClose(double close) {
		this.close.set(close);
	}

	public SimpleLongProperty timestampProperty() {
		return timestamp;
	}

	public long getTimestamp() {
		return timestamp.get();
	}

	public void setTimestamp(long timestamp) {
		this.timestamp.set(timestamp);
	}

	public SimpleLongProperty buyQtyProperty() {
		return buyQty;
	}

	public long getBuyQty() {
		return buyQty.get();
	}

	public void setBuyQty(long buyQty) {
		this.buyQty.set(buyQty);
	}

	public SimpleDoubleProperty buyPriceProperty() {
		return buyPrice;
	}

	public double getBuyPrice() {
		return buyPrice.get();
	}

	public void setBuyPrice(double buyPrice) {
		this.buyPrice.set(buyPrice);
	}

	public SimpleLongProperty sellQtyProperty() {
		return sellQty;
	}

	public long getSellQty() {
		return sellQty.get();
	}

	public void setSellQty(long sellQty) {
		this.sellQty.set(sellQty);
	}

	public SimpleDoubleProperty sellPriceProperty() {
		return sellPrice;
	}

	public double getSellPrice() {
		return sellPrice.get();
	}

	public void setSellPrice(double sellPrice) {
		this.sellPrice.set(sellPrice);
	}

	public InstrumentModel getInstrument() {
		return instrument;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(instrument, timestamp.get());
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof SummaryModel) {
			SummaryModel that = (SummaryModel) object;
			return Objects.equal(this.instrument, that.instrument)
				&& Objects.equal(this.timestamp.get(), that.timestamp.get());
		}

		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("instrument", instrument)
			.add("marketStatus", marketStatus.get())
			.add("open", open.get())
			.add("last", last.get())
			.add("close", close.get())
			.add("timestamp", timestamp.get())
			.add("buyQty", buyQty.get())
			.add("buyPrice", buyPrice.get())
			.add("sellQty", sellQty.get())
			.add("sellPrice", sellPrice.get())
			.toString();
	}

	public void update(Summary summary) {
		if (summary.getMarketStatus() != marketStatus.get()) {
			marketStatus.set(summary.getMarketStatus());
		}

		if (summary.getOpen() != open.get()) {
			open.set(summary.getOpen());
		}

		if (summary.getLast() != last.get()) {
			last.set(summary.getLast());
		}

		if (summary.getClose() != close.get()) {
			close.set(summary.getClose());
		}

		if (summary.getBuyDepthsCount() == 0) {
			buyPrice.set(0);
			buyQty.set(0);

		} else if (summary.getBuyDepthsCount() > 0) {
			Depth buyDepth = summary.getBuyDepths(0);
			log.info("update(" + instrument.getName() + ") - buyPrice == " + buyDepth.getPrice());
			if (buyDepth.getPrice() != buyPrice.get()) {
				buyPrice.set(buyDepth.getPrice());
			}

			if (buyDepth.getQuantity() != buyQty.get()) {
				buyQty.set(buyDepth.getQuantity());
			}
		}

		if (summary.getSellDepthsCount() == 0) {
			sellPrice.set(0);
			sellQty.set(0);

		} else if (summary.getSellDepthsCount() > 0) {
			Depth sellDepth = summary.getSellDepths(0);
			log.info("update(" + instrument.getName() + ") - sellPrice == " + sellDepth.getPrice());
			if (sellDepth.getPrice() != sellPrice.get()) {
				sellPrice.set(sellDepth.getPrice());
			}

			if (sellDepth.getQuantity() != sellQty.get()) {
				sellQty.set(sellDepth.getQuantity());
			}
		}

		if (summary.getTimestamp() != timestamp.get()) {
			timestamp.set(summary.getTimestamp());
		}
	}
}
