package com.kensai.gui.views.orders;

import java.time.LocalDateTime;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

import com.kensai.gui.services.ApplicationContext;
import com.kensai.gui.services.model.orders.OrderModel;
import com.kensai.gui.services.model.orders.OrdersModel;
import com.kensai.protocol.Trading.BuySell;
import com.kensai.protocol.Trading.OrderStatus;

public class OrdersViewController {

	private BorderPane root = new BorderPane();
	private TableView<OrderModel> table = new TableView<>();

	private ApplicationContext context;
	private OrdersModel model;

	public OrdersViewController(OrdersModel model) {
		this.model = model;

		initTable();
		initView();
	}

	public OrdersViewController(ApplicationContext context) {
		this(context.getModelService().getOrders());
		this.context = context;
	}

	private void initTable() {
		table.setId("orders-table");
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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
		table.getColumns().add(columnPrice);

		TableColumn<OrderModel, Number> columnQty = new TableColumn<>("Qty");
		columnQty.setCellValueFactory((cell) -> cell.getValue().quantityInitialProperty());
		table.getColumns().add(columnQty);

		TableColumn<OrderModel, Number> columnExecPrice = new TableColumn<>("ExecPrice");
		columnExecPrice.setCellValueFactory((cell) -> cell.getValue().priceExecutionProperty());
		table.getColumns().add(columnExecPrice);

		TableColumn<OrderModel, Number> columnExecQty = new TableColumn<>("ExecQty");
		columnExecQty.setCellValueFactory((cell) -> cell.getValue().quantityExecutedProperty());
		table.getColumns().add(columnExecQty);

		TableColumn<OrderModel, LocalDateTime> columnLastUpdateTime = new TableColumn<>("LastUpdate");
		columnLastUpdateTime.setCellValueFactory((cell) -> cell.getValue().lastUpdateTimeProperty());
		columnLastUpdateTime.setCellFactory(tableCell -> new OrderTimeTableCell());
		table.getColumns().add(columnLastUpdateTime);

		TableColumn<OrderModel, OrderStatus> columnStatus = new TableColumn<>("Status");
		columnStatus.setCellValueFactory((cell) -> cell.getValue().statusProperty());
		table.getColumns().add(columnStatus);

		TableColumn<OrderModel, String> columnErrorMessage = new TableColumn<>("Msg");
		columnErrorMessage.setCellValueFactory((cell) -> cell.getValue().errorMessageProperty());
		table.getColumns().add(columnErrorMessage);

		// Init rows in table
		ObservableList<OrderModel> rows = model.getOrders();
		table.setItems(rows);
	}

	private void initView() {
		root.setCenter(table);
	}

	public BorderPane getView() {
		return root;
	}

}
