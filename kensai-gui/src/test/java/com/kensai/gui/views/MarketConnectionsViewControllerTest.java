package com.kensai.gui.views;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.TilePane;

import org.junit.Before;
import org.junit.Test;

import com.kensai.gui.AbstractTestJavaFX;
import com.kensai.gui.services.ApplicationContext;
import com.kensai.gui.services.model.ModelService;
import com.kensai.gui.services.model.market.MarketConnexionModel;

public class MarketConnectionsViewControllerTest extends AbstractTestJavaFX {

	private MarketConnexionModel CAC_CONNEXION = new MarketConnexionModel("CAC", "localhost", 1664, true);

	private TilePane pane;
	private ObservableList<MarketConnexionModel> connexions;
	private ApplicationContext context;

	private MarketConnectionsViewController controller;

	@Before
	public void init() {
		ModelService modelService = mock(ModelService.class);
		context = new ApplicationContext(null, null, modelService);

		connexions = FXCollections.observableArrayList();
		when(modelService.getConnexions()).thenReturn(connexions);

		pane = new TilePane(Orientation.VERTICAL);
		controller = new MarketConnectionsViewController(context, pane);
	}

	@Test
	public void should_pane_be_empty_after_initialization() {
		assertThat(pane.getChildren()).isEmpty();
	}

	@Test
	public void should_add_connexion_in_pane_when_connexion_is_added_from_modelService() {
		// WHEN: add this connexion to model service
		connexions.add(CAC_CONNEXION);

		// THEN: Pane is updated
		assertThat(pane.getChildren()).isNotEmpty().hasSize(1);

		// AND: node is a button
		Node node = pane.getChildren().get(0);
		assertThat(node).isInstanceOf(Button.class);

		// AND: Button has same name than model's connexion name
		Button button = (Button) node;
		assertThat(button.getText()).isEqualTo(CAC_CONNEXION.getConnexionName());
	}

	@Test
	public void should_remove_connexion_in_pane_when_connexion_is_remove_from_modelService() {
		connexions.add(CAC_CONNEXION);
		assertThat(pane.getChildren()).isNotEmpty().hasSize(1);

		// WHEN: remove this connexion from model
		connexions.remove(CAC_CONNEXION);

		// THEN: Pane is updated
		assertThat(pane.getChildren()).isEmpty();
	}

}
