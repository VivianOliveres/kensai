package com.kensai.gui.views.markets;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.TilePane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactfx.EventStreams;

import com.kensai.gui.services.model.market.MarketConnectionModel;
import com.kensai.gui.views.util.DefaultOkAction;

public class MarketConnectionsEditController {
	private static Logger log = LogManager.getLogger(MarketConnectionsEditController.class);

	private MarketConnectionModel connection;

	private DefaultOkAction action = new DefaultOkAction();

	private TilePane root = new TilePane();

	private TextField connectionNameField = new TextField();
	private TextField hostField = new TextField();
	private TextField portField = new TextField();
	private CheckBox isConnectingAtStartup = new CheckBox();

	public MarketConnectionsEditController() {
		this(new MarketConnectionModel());
	}

	public MarketConnectionsEditController(MarketConnectionModel connection) {
		this.connection = connection;
		log.info("edit " + connection);

		initComponents();
		initView();
	}

	private void initComponents() {
		// Bind UI to model
		connectionNameField.setText(connection.getConnectionName());
		connection.connectionNameProperty().bind(connectionNameField.textProperty());

		hostField.setText(connection.getHost());
		connection.hostProperty().bind(hostField.textProperty());

		portField.setText(connection.getPort() + "");
		EventStreams.eventsOf(portField, KeyEvent.KEY_TYPED)
						.map(event -> portField.getText())
						.filter(text -> isInteger(text))
						.subscribe(text -> connection.setPort(Integer.valueOf(text)));

		isConnectingAtStartup.setSelected(connection.isConnectingAtStartup());
		EventStreams.eventsOf(isConnectingAtStartup, ActionEvent.ACTION)
						.map(event -> isConnectingAtStartup.isSelected())
						.subscribe(isSelected -> connection.setIsConnectingAtStartup(isSelected));
	}

	private boolean isInteger(String text) {
		try {
			Integer.parseInt(text);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	private void initView() {
		root.setPadding(new Insets(5));
		root.setVgap(4);
		root.setHgap(4);
		root.setPrefColumns(2);

		root.getChildren().add(new Label("Name: "));
		root.getChildren().add(connectionNameField);
		root.getChildren().add(new Label("Host: "));
		root.getChildren().add(hostField);
		root.getChildren().add(new Label("Port: "));
		root.getChildren().add(portField);
		root.getChildren().add(new Label("Connecting at startup: "));
		root.getChildren().add(isConnectingAtStartup);
	}

	public TilePane getView() {
		return root;
	}

	public DefaultOkAction getAction() {
		return action;
	}

	public MarketConnectionModel getConnection() {
		return connection;
	}

}
