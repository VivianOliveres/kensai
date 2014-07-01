package com.kensai.gui.views.instruments;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.action.Action;

import com.kensai.gui.services.model.instruments.InstrumentModel;
import com.kensai.gui.services.model.orders.OrderModel;
import com.kensai.gui.views.util.DefaultOkAction;
import com.kensai.gui.views.util.textfields.DoubleTextField;
import com.kensai.gui.views.util.textfields.IntegerTextField;
import com.kensai.protocol.Trading.BuySell;

public class SendOrderViewController {
	private static Logger log = LogManager.getLogger(SendOrderViewController.class);

	private DefaultOkAction action = new DefaultOkAction();

	private OrderModel order;
	private InstrumentModel instrument;

	private TilePane root = new TilePane();

	private Label instrumentLabel;
	private Label connectionLabel;
	private DoubleTextField priceField;
	private IntegerTextField qtyField;

	public SendOrderViewController(OrderModel order) {
		this.order = order;
		this.instrument = order.getInstrument();

		log.info("Open edition view for [{}] on [{}]", instrument.getName(), order.getSide());
		initComponents();
		initView();
	}

	private void initComponents() {
		instrumentLabel = new Label(instrument.getName());
		instrumentLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
		connectionLabel = new Label(instrument.getConnectionName());
		priceField = new DoubleTextField(order.priceProperty());
		qtyField = new IntegerTextField(order.quantityInitialProperty());
	}

	private void initView() {
		root.setPadding(new Insets(5));
		root.setVgap(4);
		root.setHgap(4);
		root.setPrefColumns(2);

		root.getChildren().add(new Label("Name: "));
		root.getChildren().add(instrumentLabel);
		root.getChildren().add(new Label("Connection: "));
		root.getChildren().add(connectionLabel);
		root.getChildren().add(new Label("Price: "));
		root.getChildren().add(priceField);
		root.getChildren().add(new Label("Qty: "));
		root.getChildren().add(qtyField);

		Color color = order.getSide() == BuySell.BUY ? Color.ALICEBLUE : Color.TOMATO;
		BackgroundFill fill = new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY);
		root.setBackground(new Background(fill));
	}

	public Action getAction() {
		return action;
	}

	public TilePane getView() {
		return root;
	}

	public OrderModel getOrder() {
		return order;
	}

}
