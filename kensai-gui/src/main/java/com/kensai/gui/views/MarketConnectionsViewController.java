package com.kensai.gui.views;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.reactfx.EventStreams;

import com.kensai.gui.Images;
import com.kensai.gui.services.ApplicationContext;
import com.kensai.gui.services.model.ModelService;
import com.kensai.gui.services.model.market.MarketConnexionModel;
import com.kensai.gui.services.task.TaskService;

public class MarketConnectionsViewController {
	private static Logger log = LogManager.getLogger(MarketConnectionsViewController.class);

	private BorderPane root = new BorderPane();
	private ListView<MarketConnexionModel> connexionsList;

	private ModelService modelService;
	private TaskService taskService;

	public MarketConnectionsViewController(ApplicationContext context) {
		this(context, new ListView<>());
	}

	public MarketConnectionsViewController(ApplicationContext context, ListView<MarketConnexionModel> connexionsList) {
		this.modelService = context.getModelService();
		this.taskService = context.getTaskService();
		this.connexionsList = connexionsList;

		initView();
	}

	private void initView() {
		connexionsList.setItems(modelService.getConnexions());
		connexionsList.setCellFactory(list -> new ConnextionListCell());
		root.setCenter(connexionsList);

		HBox buttonsBox = new HBox(5);
		buttonsBox.getChildren().addAll(createAddButton(), createEditButton(), createRemoveButton());
		root.setBottom(buttonsBox);
	}

	private Button createAddButton() {
		ImageView addView = new ImageView(Images.ADD);
		Button buttonAdd = new Button("", addView);

		// Add connection on click
		EventStreams.eventsOf(buttonAdd, ActionEvent.ACTION)
						.map(event -> new MarketConnexionModel())
						.map(connection -> doOnClick(connection, "Add Market Connection"))
						.filter(connection -> connection != null)
						.subscribe(connection -> {
							log.info("Add connection: " + connection);
							modelService.getConnexions().add(connection);
						});
		
		return buttonAdd;
	}

	private MarketConnexionModel doOnClick(MarketConnexionModel connection, String dialogTitle) {
		MarketConnectionsEditController controller = new MarketConnectionsEditController(connection);

		Dialog dialog = new Dialog(root, dialogTitle);
		dialog.setResizable(false);
		dialog.setIconifiable(false);
		dialog.setContent(controller.getView());
		dialog.getActions().addAll(controller.getAction(), Dialog.Actions.CANCEL);

		Action answer = dialog.show();
		if (answer.equals(Dialog.Actions.CANCEL)) {
			return null;
		}

		return controller.getConnection();
	}

	private Button createEditButton() {
		ImageView editView = new ImageView(Images.EDIT);
		Button buttonEdit = new Button("", editView);

		// Edit connection on click
		EventStreams.eventsOf(buttonEdit, ActionEvent.ACTION)
						.filter(event -> connexionsList.getSelectionModel().getSelectedItem() != null)
						.map(event -> connexionsList.getSelectionModel().getSelectedItem())
						.map(connection -> new MarketConnexionModel(connection))
						.map(connection -> doOnClick(connection, "Edit Market Connection"))
						.filter(connection -> connection != null)
						.subscribe(connection -> {
							log.info("Edit connection: " + connection);
							MarketConnexionModel toEditItem = connexionsList.getSelectionModel().getSelectedItem();
							toEditItem.setConnexionName(connection.getConnexionName());
							toEditItem.setHost(connection.getHost());
							toEditItem.setPort(connection.getPort());
							toEditItem.setIsConnectingAtStartup(connection.isConnectingAtStartup());
						});

		return buttonEdit;
	}

	private Button createRemoveButton() {
		ImageView removeView = new ImageView(Images.REMOVE);
		Button buttonRemove = new Button("", removeView);

		// Remove connection on click
		EventStreams.eventsOf(buttonRemove, ActionEvent.ACTION)
						.map(event -> connexionsList.getSelectionModel().getSelectedItem())
						.map(selectedItem -> Dialogs.create()
													       .owner( connexionsList)
													       .title("Remove Market Connection")
													       .message( "Do you want to remove connexion [" + selectedItem.getConnexionName() + "]")
													       .showConfirm())
						.filter(response -> Dialog.Actions.YES.equals(response))
						.map(response -> connexionsList.getSelectionModel().getSelectedItem())
						.subscribe(selectedItem -> {
							log.info("Remove connection: " + selectedItem); 
							modelService.getConnexions().remove(selectedItem);
						});

		// Disable button when nothing is selected
		ReadOnlyIntegerProperty selectedIndexProperty = connexionsList.getSelectionModel().selectedIndexProperty();
		buttonRemove.disableProperty().bind(Bindings.greaterThan(0, selectedIndexProperty));

		return buttonRemove;
	}

	public BorderPane getView() {
		return root;
	}
}
