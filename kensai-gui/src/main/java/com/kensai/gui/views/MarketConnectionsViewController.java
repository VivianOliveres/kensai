package com.kensai.gui.views;

import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import com.kensai.gui.Images;
import com.kensai.gui.services.ApplicationContext;
import com.kensai.gui.services.model.ModelService;
import com.kensai.gui.services.model.market.MarketConnexionModel;
import com.kensai.gui.services.task.TaskService;

public class MarketConnectionsViewController {

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
		// TODO
		return buttonAdd;
	}

	private Button createEditButton() {
		ImageView editView = new ImageView(Images.EDIT);
		Button buttonEdit = new Button("", editView);
		// TODO
		return buttonEdit;
	}

	private Button createRemoveButton() {
		ImageView removeView = new ImageView(Images.REMOVE);
		Button buttonRemove = new Button("", removeView);
		// TODO
		return buttonRemove;
	}

	public BorderPane getView() {
		return root;
	}
}
