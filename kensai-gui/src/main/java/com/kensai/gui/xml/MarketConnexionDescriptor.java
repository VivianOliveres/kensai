package com.kensai.gui.xml;import com.google.common.base.Objects;

public class MarketConnexionDescriptor {

	private String connectionName;
	private String host;
	private int port;
	private boolean isConnectingAtStartup;

	public MarketConnexionDescriptor() {
		// No args constructor for deserializer
	}
	
	public MarketConnexionDescriptor(String connectionName, String host, int port, boolean isConnectingAtStartup) {
		this.connectionName = connectionName;
		this.host = host;
		this.port = port;
		this.isConnectingAtStartup = isConnectingAtStartup;
	}

	public String getConnectionName() {
		return connectionName;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public boolean isConnectingAtStartup() {
		return isConnectingAtStartup;
	}

	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setConnectingAtStartup(boolean isConnectingAtStartup) {
		this.isConnectingAtStartup = isConnectingAtStartup;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(connectionName);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof MarketConnexionDescriptor) {
			MarketConnexionDescriptor that = (MarketConnexionDescriptor) object;
			return Objects.equal(this.connectionName, that.connectionName);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("connectionName", connectionName)
			.add("host", host)
			.add("port", port)
			.add("isConnectingAtStartup", isConnectingAtStartup)
			.toString();
	}

}
