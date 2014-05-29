package com.kensai.gui.views.instruments;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

import com.kensai.gui.services.ApplicationContext;
import com.kensai.gui.services.model.instruments.InstrumentModel;
import com.kensai.protocol.Trading.MarketStatus;

public class InstrumentsViewController {

	private BorderPane root = new BorderPane();
	private TableView<InstrumentModel> table = new TableView<>();

	private ApplicationContext context;

	public InstrumentsViewController(ApplicationContext context) {
		this.context = context;

		initTable();
		initView();
	}

	private void initTable() {
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		TableColumn<InstrumentModel, String> columnIsin = new TableColumn<>("Isin");
		columnIsin.setCellValueFactory((cell) -> cell.getValue().isinProperty());
		table.getColumns().add(columnIsin);

		TableColumn<InstrumentModel, String> columnName = new TableColumn<>("Name");
		columnName.setCellValueFactory((cell) -> cell.getValue().nameProperty());
		table.getColumns().add(columnName);

		// Not used today
		// TableColumn<InstrumentModel, String> columnMarketName = new TableColumn<>("Market");
		// columnMarketName.setCellValueFactory((cell) -> cell.getValue().marketProperty());
		// table.getColumns().add(columnMarketName);

		TableColumn<InstrumentModel, String> columnConnectionName = new TableColumn<>("Connection");
		columnConnectionName.setCellValueFactory((cell) -> cell.getValue().connectionNameProperty());
		table.getColumns().add(columnConnectionName);

		TableColumn<InstrumentModel, String> columnType = new TableColumn<>("Type");
		columnType.setCellValueFactory((cell) -> cell.getValue().typeProperty());
		table.getColumns().add(columnType);

		TableColumn<InstrumentModel, MarketStatus> columnMarketStatus = new TableColumn<>("Status");
		columnMarketStatus.setCellValueFactory((cell) -> cell.getValue().getSummary().marketStatusProperty());
		table.getColumns().add(columnMarketStatus);

		TableColumn<InstrumentModel, Number> columnOpen = new TableColumn<>("Open");
		columnOpen.setCellValueFactory((cell) -> cell.getValue().getSummary().openProperty());
		table.getColumns().add(columnOpen);

		TableColumn<InstrumentModel, Number> columnLast = new TableColumn<>("Last");
		columnLast.setCellValueFactory((cell) -> cell.getValue().getSummary().lastProperty());
		table.getColumns().add(columnLast);

		TableColumn<InstrumentModel, Number> columnClose = new TableColumn<>("Close");
		columnClose.setCellValueFactory((cell) -> cell.getValue().getSummary().closeProperty());
		table.getColumns().add(columnClose);

		TableColumn<InstrumentModel, Number> columnBuyQty = new TableColumn<>("BuyQty");
		columnBuyQty.setCellValueFactory((cell) -> cell.getValue().getSummary().buyQtyProperty());
		table.getColumns().add(columnBuyQty);

		TableColumn<InstrumentModel, Number> columnBuyPrice = new TableColumn<>("Buy");
		columnBuyPrice.setCellValueFactory((cell) -> cell.getValue().getSummary().buyPriceProperty());
		table.getColumns().add(columnBuyPrice);

		TableColumn<InstrumentModel, Number> columnSellPrice = new TableColumn<>("Sell");
		columnSellPrice.setCellValueFactory((cell) -> cell.getValue().getSummary().sellPriceProperty());
		table.getColumns().add(columnSellPrice);

		TableColumn<InstrumentModel, Number> columnSellQty = new TableColumn<>("SellQty");
		columnSellQty.setCellValueFactory((cell) -> cell.getValue().getSummary().sellQtyProperty());
		table.getColumns().add(columnSellQty);

		// Init rows in table
		ObservableList<InstrumentModel> rows = context.getModelService().getInstruments().getInstruments();
		table.setItems(rows);
	}

	private void initView() {
		root.setCenter(table);
	}

	public BorderPane getView() {
		return root;
	}

}
