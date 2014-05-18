package com.kensai.gui.services.model.market;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import com.google.common.base.Objects;
import com.kensai.gui.xml.MarketConnexionDescriptor;

public class MarketConnexionModel {

	private SimpleStringProperty connexionName = new SimpleStringProperty();
	private SimpleStringProperty host = new SimpleStringProperty();
	private SimpleIntegerProperty port = new SimpleIntegerProperty();
	private SimpleBooleanProperty isConnectingAtStartup = new SimpleBooleanProperty();
	private SimpleObjectProperty<ConnectionState> connection = new SimpleObjectProperty<>();

	public MarketConnexionModel() {
		// New instance
	}

	public MarketConnexionModel(MarketConnexionModel copy) {
		this.connexionName.setValue(copy.getConnexionName());
		this.host.setValue(copy.getHost());
		this.port.setValue(copy.getPort());
		this.isConnectingAtStartup.setValue(copy.isConnectingAtStartup());
		this.connection.setValue(copy.getConnectionState());
	}

	public MarketConnexionModel(MarketConnexionDescriptor desc) {
		this(desc.getConnectionName(), desc.getHost(), desc.getPort(), desc.isConnectingAtStartup());
	}

	public MarketConnexionModel(String connexionName, String host, int port, boolean isConnectingAtStartup) {
		this.connexionName.setValue(connexionName);
		this.host.setValue(host);
		this.port.setValue(port);
		this.isConnectingAtStartup.setValue(isConnectingAtStartup);
		this.connection.setValue(ConnectionState.DECONNECTED);
	}

	public String getConnexionName() {
		return connexionName.get();
	}

	public String getHost() {
		return host.get();
	}

	public int getPort() {
		return port.get();
	}

	public boolean isConnectingAtStartup() {
		return isConnectingAtStartup.get();
	}

	public ConnectionState getConnectionState() {
		return connection.get();
	}

	public void setConnexionName(String name) {
		this.connexionName.set(name);
	}

	public void setHost(String host) {
		this.host.set(host);
	}

	public void setPort(int port) {
		this.port.set(port);
	}

	public void setIsConnectingAtStartup(boolean isConnectingAtStartup) {
		this.isConnectingAtStartup.set(isConnectingAtStartup);
	}

	public void setConnectionState(ConnectionState state) {
		this.connection.set(state);
	}

	public StringProperty connectionNameProperty() {
		return connexionName;
	}

	public StringProperty hostProperty() {
		return host;
	}

	public IntegerProperty portProperty() {
		return port;
	}

	public BooleanProperty isConnectingAtStartupProperty() {
		return isConnectingAtStartup;
	}

	public SimpleObjectProperty<ConnectionState> connectionStateProperty() {
		return connection;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(connexionName);
	}

	@Override
	public boolean equals(Object object){
		if (object instanceof MarketConnexionModel) {
			MarketConnexionModel that = (MarketConnexionModel) object;
			return Objects.equal(this.connexionName, that.connexionName);
		}

		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("connexionName", connexionName.get())
			.add("host", host.get())
			.add("port", port.get())
			.add("isConnectingAtStartup", isConnectingAtStartup.get())
			.toString();
	}
	
	public MarketConnexionDescriptor toDescriptor() {
		return new MarketConnexionDescriptor(getConnexionName(), getHost(), getPort(), isConnectingAtStartup());
	}

}
