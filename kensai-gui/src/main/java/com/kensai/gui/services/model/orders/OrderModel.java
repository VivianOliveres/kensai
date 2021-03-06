package com.kensai.gui.services.model.orders;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import com.google.common.base.Objects;
import com.kensai.gui.services.model.instruments.InstrumentModel;
import com.kensai.protocol.Trading.BuySell;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.OrderAction;
import com.kensai.protocol.Trading.OrderStatus;

public class OrderModel {

	private final SimpleLongProperty id = new SimpleLongProperty();
	private final SimpleObjectProperty<BuySell> side = new SimpleObjectProperty<>();
	private final InstrumentModel instrument;
	private final SimpleDoubleProperty price = new SimpleDoubleProperty();
	private final SimpleDoubleProperty priceExecution = new SimpleDoubleProperty();
	private final SimpleIntegerProperty quantityInitial = new SimpleIntegerProperty();
	private final SimpleIntegerProperty quantityExecuted = new SimpleIntegerProperty();
	private final SimpleObjectProperty<LocalDateTime> insertTime = new SimpleObjectProperty<>();
	private final SimpleObjectProperty<LocalDateTime> lastUpdateTime = new SimpleObjectProperty<>();
	private final SimpleObjectProperty<OrderStatus> status = new SimpleObjectProperty<>();
	private final SimpleStringProperty userData = new SimpleStringProperty();
	private final SimpleStringProperty errorMessage = new SimpleStringProperty();
	private final SimpleObjectProperty<OrderAction> orderAction = new SimpleObjectProperty<>();

	public OrderModel(Order order, InstrumentModel instrument) {
		this.id.set(order.getId());
		this.side.set(order.getSide());
		this.instrument = instrument;
		this.price.set(order.getPrice());
		this.priceExecution.set(order.getExecPrice());
		this.quantityInitial.set(order.getInitialQuantity());
		this.quantityExecuted.set(order.getExecutedQuantity());
		this.insertTime.set(convertTime(order.getInsertTime()));
		this.lastUpdateTime.set(convertTime(order.getLastUpdateTime()));
		this.status.set(order.getOrderStatus());
		this.userData.set(order.getUserData());
		this.errorMessage.set(order.getErrorMessage());
		this.orderAction.set(order.getAction());
	}

	public OrderModel(InstrumentModel instrument, BuySell side, double price, int qty) {
		this(instrument, 0L, side, price, qty);
	}

	public OrderModel(InstrumentModel instrument, long id, BuySell side, double price, int qty) {
		this.id.set(id);
		this.side.set(side);
		this.instrument = instrument;
		this.price.set(price);
		this.priceExecution.set(price);
		this.quantityInitial.set(qty);
		this.quantityExecuted.set(0);
		this.insertTime.set(LocalDateTime.now());
		this.lastUpdateTime.set(LocalDateTime.now());
		this.status.set(OrderStatus.ON_MARKET);
		this.userData.set("");
		this.errorMessage.set("");
		this.orderAction.set(OrderAction.INSERT);
	}

	public OrderModel(OrderModel item) {
		this.id.set(item.getId());
		this.side.set(item.getSide());
		this.instrument = item.getInstrument();
		this.price.set(item.getPrice());
		this.priceExecution.set(item.getPriceExecution());
		this.quantityInitial.set(item.getQuantityInitial());
		this.quantityExecuted.set(item.getQuantityExecuted());
		this.insertTime.set(item.getInsertTime());
		this.lastUpdateTime.set(item.getLastUpdateTime());
		this.status.set(item.getStatus());
		this.userData.set(item.getUserData());
		this.errorMessage.set(item.getErrorMessage());
		this.orderAction.set(item.getOrderAction());
	}

	private LocalDateTime convertTime(long milli) {
		Instant instant = Instant.ofEpochMilli(milli);
		return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
	}

	private long convertTime(LocalDateTime date) {
		Instant instant = date.toInstant(ZoneOffset.UTC);
		return instant.toEpochMilli();
	}

	public SimpleLongProperty idProperty() {
		return id;
	}

	public long getId() {
		return id.get();
	}

	public SimpleObjectProperty<BuySell> sideProperty() {
		return side;
	}

	public BuySell getSide() {
		return side.get();
	}

	public InstrumentModel getInstrument() {
		return instrument;
	}

	public SimpleDoubleProperty priceProperty() {
		return price;
	}

	public double getPrice() {
		return price.get();
	}

	public void setPrice(double price) {
		this.price.set(price);
	}

	public SimpleDoubleProperty priceExecutionProperty() {
		return priceExecution;
	}

	public double getPriceExecution() {
		return priceExecution.get();
	}

	public void setPriceExecution(double priceExecution) {
		this.priceExecution.set(priceExecution);
	}

	public SimpleIntegerProperty quantityInitialProperty() {
		return quantityInitial;
	}

	public int getQuantityInitial() {
		return quantityInitial.get();
	}

	public void setQuantityInitial(int quantityInitial) {
		this.quantityInitial.set(quantityInitial);
	}

	public SimpleIntegerProperty quantityExecutedProperty() {
		return quantityExecuted;
	}

	public int getQuantityExecuted() {
		return quantityExecuted.get();
	}

	public void setQuantityExecuted(int quantityExecuted) {
		this.quantityExecuted.set(quantityExecuted);
	}

	public SimpleObjectProperty<LocalDateTime> lastUpdateTimeProperty() {
		return lastUpdateTime;
	}

	public LocalDateTime getLastUpdateTime() {
		return lastUpdateTime.get();
	}

	public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
		this.lastUpdateTime.set(lastUpdateTime);
	}

	public SimpleObjectProperty<LocalDateTime> insertTimeProperty() {
		return insertTime;
	}

	public LocalDateTime getInsertTime() {
		return insertTime.get();
	}

	public void setInsertTime(LocalDateTime insertTime) {
		this.insertTime.set(insertTime);
	}
	public SimpleObjectProperty<OrderStatus> statusProperty() {
		return status;
	}

	public OrderStatus getStatus() {
		return status.get();
	}

	public void setStatus(OrderStatus status) {
		this.status.set(status);
	}

	public SimpleStringProperty errorMessageProperty() {
		return errorMessage;
	}

	public String getErrorMessage() {
		return errorMessage.get();
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage.set(errorMessage);
	}

	public SimpleStringProperty userDataProperty() {
		return userData;
	}

	public String getUserData() {
		return userData.get();
	}

	public void setUserData(String userData) {
		this.userData.set(userData);
	}

	public SimpleObjectProperty<OrderAction> orderActionProperty() {
		return orderAction;
	}

	public OrderAction getOrderAction() {
		return orderAction.get();
	}

	public void setUserData(OrderAction orderAction) {
		this.orderAction.set(orderAction);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id.get());
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof OrderModel) {
			OrderModel that = (OrderModel) object;
			return Objects.equal(this.id.get(), that.id.get()) &&
				    Objects.equal(this.instrument, that.instrument);
		}

		return false;
	}

	public boolean equals(Order order) {
		return order != null && order.getId() == id.get() && order.getInstrument().getIsin().equals(instrument.getIsin());
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
						  .add("id", id.get())
						  .add("side", side.get())
						  .add("instrument", instrument.getName())
						  .add("price", price.get())
						  .add("priceExecution", priceExecution.get())
						  .add("quantityInitial", quantityInitial.get())
						  .add("quantityExecuted", quantityExecuted.get())
						  .add("insertTime", insertTime.get())
						  .add("lastUpdateTime", lastUpdateTime.get())
						  .add("status", status.get())
						  .add("errorMessage", errorMessage.get())
						  .toString();
	}

	public void update(Order order) {
		this.price.set(order.getPrice());
		this.priceExecution.set(order.getExecPrice());
		this.quantityInitial.set(order.getInitialQuantity());
		this.quantityExecuted.set(order.getExecutedQuantity());
		this.status.set(order.getOrderStatus());
		this.errorMessage.set(order.getErrorMessage());
		this.lastUpdateTime.set(convertTime(order.getLastUpdateTime()));
	}

	public Order.Builder toOrder() {
		// User should be add
		return Order.newBuilder()
						.setInstrument(getInstrument().toInstrument())
						.setId(getId())
						.setSide(getSide())
						.setPrice(getPrice())
						.setExecPrice(getPriceExecution())
						.setInitialQuantity(getQuantityInitial())
						.setExecutedQuantity(getQuantityExecuted())
						.setInsertTime(convertTime(getInsertTime()))
						.setLastUpdateTime(convertTime(getLastUpdateTime()))
						.setErrorMessage(getErrorMessage())
						.setOrderStatus(getStatus())
						.setUserData(getUserData())
						.setAction(getOrderAction());
	}
}
