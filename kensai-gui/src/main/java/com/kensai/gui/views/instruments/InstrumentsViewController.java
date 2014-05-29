package com.kensai.gui.views.instruments;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

import com.kensai.gui.services.ApplicationContext;
import com.kensai.gui.services.model.instruments.InstrumentModel;

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
