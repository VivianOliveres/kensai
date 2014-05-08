package com.kensai.gui.views;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;

import org.reactfx.EventStreams;

import com.kensai.gui.services.ApplicationContext;
import com.kensai.gui.services.model.ModelService;
import com.kensai.gui.services.model.market.MarketConnexionModel;
import com.kensai.gui.services.task.TaskService;

public class MarketConnectionsViewController {

	private BorderPane root = new BorderPane();
	private FlowPane flowPane;

	private ObservableList<Button> marketButtons = FXCollections.observableArrayList();

	private ModelService modelService;
	private TaskService taskService;

	public MarketConnectionsViewController(ApplicationContext context) {
		this(context, new FlowPane());
	}

	public MarketConnectionsViewController(ApplicationContext context, FlowPane flowPane) {
		this.modelService = context.getModelService();
		this.taskService = context.getTaskService();
		this.flowPane = flowPane;

		initView();
		initDatas();
	}

	private void initView() {
		ScrollPane pane = new ScrollPane(flowPane);
		EventStreams.changesOf(marketButtons).subscribe(change -> doChangeOnMarketButtons(change));
		root.setCenter(pane);
	}

	private Void doChangeOnMarketButtons(Change<? extends Button> change) {
		while (change.next()) {
			for (Button button : change.getRemoved()) {
				flowPane.getChildren().remove(button);
			}
			
			for (Button button : change.getAddedSubList()) {
				flowPane.getChildren().add(button);
			}
		}

		return null;
	}
	
	private void initDatas() {
		ObservableList<MarketConnexionModel> connexions = modelService.getConnexions();

		EventStreams.changesOf(connexions).subscribe(change -> doChangeOnConnexions(change));

		// First initilization
		connexions.forEach(connexion -> marketButtons.add(createButton(connexion)));
	}

	private Void doChangeOnConnexions(Change<? extends MarketConnexionModel> change) {
		while (change.next()) {
			change.getRemoved().forEach(connexion -> 
					 	{
					 		Button connexionButton = marketButtons.stream()
									 											.filter(button -> button.getText().equals(connexion.getConnexionName()))
									 											.findFirst()
									 											.get();
					 		marketButtons.remove(connexionButton);
						});

			change.getAddedSubList().forEach(connexion -> marketButtons.add(createButton(connexion)));
		}

		return null;
	}

	private Button createButton(MarketConnexionModel connexion) {
		ImageView view = new ImageView(connexion.getConnectionState().getImage());
		Button button = new Button(connexion.getConnexionName(), view);

		EventStreams.changesOf(connexion.connectionStateProperty()).subscribe(change -> view.setImage(change.getNewValue().getImage()));

		return button;
	}

	public BorderPane getView() {
		return root;
	}
}
