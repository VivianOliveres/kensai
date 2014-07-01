package com.kensai.gui.views.orders;

import java.time.LocalDateTime;

import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kensai.gui.actions.OrderDeleteAction;
import com.kensai.gui.actions.OrderUpdateAction;
import com.kensai.gui.services.ApplicationContext;
import com.kensai.gui.services.connectors.MarketConnector;
import com.kensai.gui.services.model.orders.OrderModel;
import com.kensai.gui.services.model.orders.OrdersModel;
import com.kensai.gui.views.util.FlashingTableCell;
import com.kensai.protocol.Trading.BuySell;
import com.kensai.protocol.Trading.OrderStatus;

public class OrdersViewController {
	private static final Logger log = LogManager.getLogger(OrdersViewController.class);

	private BorderPane root = new BorderPane();
	private TableView<OrderModel> table = new TableView<>();

	private ApplicationContext context;
	private OrdersModel model;

	public OrdersViewController(OrdersModel model) {
		this.model = model;

		initTable();
		initView();
		initContextualMenu();
	}

	public OrdersViewController(ApplicationContext context) {
		this(context.getModelService().getOrders());
		this.context = context;
	}

	private void initTable() {
		table.setId("orders-table");
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		// Init click acction on Table
		table.setRowFactory(table -> createRow());

		// Init columns
		TableColumn<OrderModel, Number> columnId = new TableColumn<>("Id");
		columnId.setCellValueFactory((cell) -> cell.getValue().idProperty());
		table.getColumns().add(columnId);

		TableColumn<OrderModel, LocalDateTime> columnInsertTime = new TableColumn<>("Insert");
		columnInsertTime.setCellValueFactory((cell) -> cell.getValue().insertTimeProperty());
		columnInsertTime.setCellFactory(tableCell -> new OrderTimeTableCell());
		table.getColumns().add(columnInsertTime);

		TableColumn<OrderModel, BuySell> columnSide = new TableColumn<>("Side");
		columnSide.setCellValueFactory((cell) -> cell.getValue().sideProperty());
		table.getColumns().add(columnSide);

		TableColumn<OrderModel, String> columnInstrumentName = new TableColumn<>("Instrument");
		columnInstrumentName.setCellValueFactory((cell) -> cell.getValue().getInstrument().nameProperty());
		table.getColumns().add(columnInstrumentName);

		TableColumn<OrderModel, Number> columnPrice = new TableColumn<>("Price");
		columnPrice.setCellValueFactory((cell) -> cell.getValue().priceProperty());
		columnPrice.setCellFactory(column -> new FlashingTableCell<>());
		table.getColumns().add(columnPrice);

		TableColumn<OrderModel, Number> columnQty = new TableColumn<>("Qty");
		columnQty.setCellValueFactory((cell) -> cell.getValue().quantityInitialProperty());
		columnQty.setCellFactory(column -> new FlashingTableCell<>());
		table.getColumns().add(columnQty);

		TableColumn<OrderModel, Number> columnExecPrice = new TableColumn<>("ExecPrice");
		columnExecPrice.setCellValueFactory((cell) -> cell.getValue().priceExecutionProperty());
		columnExecPrice.setCellFactory(column -> new FlashingTableCell<>());
		table.getColumns().add(columnExecPrice);

		TableColumn<OrderModel, Number> columnExecQty = new TableColumn<>("ExecQty");
		columnExecQty.setCellValueFactory((cell) -> cell.getValue().quantityExecutedProperty());
		columnExecQty.setCellFactory(column -> new FlashingTableCell<>());
		table.getColumns().add(columnExecQty);

		TableColumn<OrderModel, LocalDateTime> columnLastUpdateTime = new TableColumn<>("LastUpdate");
		columnLastUpdateTime.setCellValueFactory((cell) -> cell.getValue().lastUpdateTimeProperty());
		columnLastUpdateTime.setCellFactory(tableCell -> new OrderTimeTableCell());
		table.getColumns().add(columnLastUpdateTime);

		TableColumn<OrderModel, OrderStatus> columnStatus = new TableColumn<>("Status");
		columnStatus.setCellValueFactory((cell) -> cell.getValue().statusProperty());
		columnStatus.setCellFactory(column -> new FlashingTableCell<>());
		table.getColumns().add(columnStatus);

		TableColumn<OrderModel, String> columnErrorMessage = new TableColumn<>("Msg");
		columnErrorMessage.setCellValueFactory((cell) -> cell.getValue().errorMessageProperty());
		columnErrorMessage.setCellFactory(column -> new FlashingTableCell<>());
		table.getColumns().add(columnErrorMessage);

		// Init rows in table
		ObservableList<OrderModel> rows = model.getOrders();
		table.setItems(rows);
	}

	private TableRow<OrderModel> createRow() {
		TableRow<OrderModel> row = new TableRow<>();
		row.setOnMouseClicked(event -> doMouseClicked(event));
		return row;
	}

	private void doMouseClicked(MouseEvent event) {
		if (event.getClickCount() < 2) {
			return;
		}

		TableRow<OrderModel> row = (TableRow<OrderModel>) event.getSource();
		OrderModel order = new OrderModel(row.getItem());
		MarketConnector connector = context.getModelService().getConnectorService().getConnector(order.getInstrument());
		new OrderUpdateAction(root, order, connector).execute();
	}

	private void initView() {
		root.setCenter(table);
	}

	private void initContextualMenu() {
		MenuItem menuUpdate = new MenuItem("Update order");
		menuUpdate.setOnAction(event -> {
			OrderModel orderModel = table.getSelectionModel().getSelectedItem();
			if (!orderModel.getStatus().equals(OrderStatus.ON_MARKET)) {
				log.info("Can not update a [{}] order", orderModel.getStatus());
				return;
			}

			OrderModel order = new OrderModel(orderModel);
			MarketConnector connector = context.getModelService().getConnectorService().getConnector(order.getInstrument());
			new OrderUpdateAction(root, order, connector).execute();
		});

		MenuItem menuDelete = new MenuItem("Delete order");
		menuDelete.setOnAction(event -> {
			OrderModel orderModel = table.getSelectionModel().getSelectedItem();
			if (!orderModel.getStatus().equals(OrderStatus.ON_MARKET)) {
				log.info("Can not delete a [{}] order", orderModel.getStatus());
				return;
			}

			OrderModel order = new OrderModel(orderModel);
			MarketConnector connector = context.getModelService().getConnectorService().getConnector(order.getInstrument());
			new OrderDeleteAction(root, order, connector).execute();
		});

		ContextMenu menu = new ContextMenu();
		menu.getItems().addAll(menuUpdate, menuDelete);
		table.setContextMenu(menu);
	}

	public BorderPane getView() {
		return root;
	}

}
