package com.kensai.gui.views.instruments;

import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kensai.gui.actions.OrderInsertAction;
import com.kensai.gui.services.ApplicationContext;
import com.kensai.gui.services.connectors.MarketConnector;
import com.kensai.gui.services.model.instruments.InstrumentModel;
import com.kensai.gui.services.model.instruments.SummaryModel;
import com.kensai.gui.services.model.orders.OrderModel;
import com.kensai.gui.views.util.FlashingTableCell;
import com.kensai.protocol.Trading.BuySell;
import com.kensai.protocol.Trading.MarketStatus;

public class InstrumentsViewController {
	private static final Logger log = LogManager.getLogger(InstrumentsViewController.class);

	private BorderPane root = new BorderPane();
	private TableView<InstrumentModel> table = new TableView<>();

	private ApplicationContext context;

	public InstrumentsViewController(ApplicationContext context) {
		this.context = context;

		initTable();
		initView();
	}

	private void initTable() {
		table.setId("instruments-table");
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
		columnMarketStatus.setCellFactory(column -> new FlashingTableCell<>());
		table.getColumns().add(columnMarketStatus);

		TableColumn<InstrumentModel, Number> columnOpen = new TableColumn<>("Open");
		columnOpen.setCellValueFactory((cell) -> cell.getValue().getSummary().openProperty());
		columnOpen.setCellFactory(column -> new FlashingTableCell<>());
		table.getColumns().add(columnOpen);

		TableColumn<InstrumentModel, Number> columnLast = new TableColumn<>("Last");
		columnLast.setCellValueFactory((cell) -> cell.getValue().getSummary().lastProperty());
		columnLast.setCellFactory(column -> new FlashingTableCell<>());
		table.getColumns().add(columnLast);

		TableColumn<InstrumentModel, Number> columnClose = new TableColumn<>("Close");
		columnClose.setCellValueFactory((cell) -> cell.getValue().getSummary().closeProperty());
		columnClose.setCellFactory(column -> new FlashingTableCell<>());
		table.getColumns().add(columnClose);

		TableColumn<InstrumentModel, Number> columnBuyQty = new TableColumn<>("BuyQty");
		columnBuyQty.setCellValueFactory((cell) -> cell.getValue().getSummary().buyQtyProperty());
		columnBuyQty.setCellFactory(column -> createEnhancedFlashingTableCell(BuySell.BUY));
		table.getColumns().add(columnBuyQty);

		TableColumn<InstrumentModel, Number> columnBuyPrice = new TableColumn<>("Buy");
		columnBuyPrice.setCellValueFactory((cell) -> cell.getValue().getSummary().buyPriceProperty());
		columnBuyPrice.setCellFactory(column -> createEnhancedFlashingTableCell(BuySell.BUY));
		table.getColumns().add(columnBuyPrice);

		TableColumn<InstrumentModel, Number> columnSellPrice = new TableColumn<>("Sell");
		columnSellPrice.setCellValueFactory((cell) -> cell.getValue().getSummary().sellPriceProperty());
		columnSellPrice.setCellFactory(column -> createEnhancedFlashingTableCell(BuySell.SELL));
		table.getColumns().add(columnSellPrice);

		TableColumn<InstrumentModel, Number> columnSellQty = new TableColumn<>("SellQty");
		columnSellQty.setCellValueFactory((cell) -> cell.getValue().getSummary().sellQtyProperty());
		columnSellQty.setCellFactory(column -> createEnhancedFlashingTableCell(BuySell.SELL));
		table.getColumns().add(columnSellQty);

		// Init rows in table
		ObservableList<InstrumentModel> rows = context.getModelService().getInstruments().getInstruments();
		table.setItems(rows);
	}

	private FlashingTableCell createEnhancedFlashingTableCell(BuySell side) {
		FlashingTableCell cell = new FlashingTableCell<>();
		cell.setOnMouseClicked(event -> doMouseClicked(event, side));
		return cell;
	}

	private void doMouseClicked(MouseEvent event, BuySell side) {
		if (event.getClickCount() < 2) {
			return;
		}

		TableCell cell = (TableCell) event.getSource();
		InstrumentModel instrument = (InstrumentModel) cell.getTableRow().getItem();
		MarketConnector connector = context.getModelService().getConnectorService().getConnector(instrument.getConnectionName());

		// Initialize new order
		SummaryModel summary = instrument.getSummary();
		double price = side == BuySell.BUY ? summary.getBuyPrice() : summary.getSellPrice();
		OrderModel newOrder = new OrderModel(instrument, 0L, side, price, 1);
		new OrderInsertAction(root, newOrder, connector).execute();
	}

	private void initView() {
		root.setCenter(table);
	}

	public BorderPane getView() {
		return root;
	}

}
