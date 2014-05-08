package com.kensai.gui.views;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

import org.junit.Before;
import org.junit.Test;

import com.kensai.gui.AbstractTestJavaFX;
import com.kensai.gui.services.ApplicationContext;
import com.kensai.gui.services.model.ModelService;
import com.kensai.gui.services.model.market.MarketConnexionModel;

public class MarketConnectionsViewControllerTest extends AbstractTestJavaFX {

	private MarketConnexionModel CAC_CONNEXION = new MarketConnexionModel("CAC", "localhost", 1664, true);

	private ListView<MarketConnexionModel> connexionsList;
	private ObservableList<MarketConnexionModel> connexions;
	private ApplicationContext context;

	private MarketConnectionsViewController controller;

	@Before
	public void init() {
		ModelService modelService = mock(ModelService.class);
		context = new ApplicationContext(null, null, modelService);

		connexions = FXCollections.observableArrayList();
		when(modelService.getConnexions()).thenReturn(connexions);

		connexionsList = new ListView<>();
		controller = new MarketConnectionsViewController(context, connexionsList);
	}

	@Test
	public void should_pane_be_empty_after_initialization() {
		assertThat(connexionsList.getItems()).isEmpty();
	}

	@Test
	public void should_add_connexion_in_pane_when_connexion_is_added_from_modelService() {
		// WHEN: add this connexion to model service
		connexions.add(CAC_CONNEXION);

		// THEN: ListView is updated
		assertThat(connexionsList.getItems()).isNotEmpty().hasSize(1);

		// AND: Item in list is same than in ModelService
		MarketConnexionModel item = connexionsList.getItems().get(0);
		assertThat(item).isEqualTo(CAC_CONNEXION);
	}

	@Test
	public void should_remove_connexion_in_pane_when_connexion_is_remove_from_modelService() {
		connexions.add(CAC_CONNEXION);
		assertThat(connexionsList.getItems()).isNotEmpty().hasSize(1);

		// WHEN: remove this connexion from model
		connexions.remove(CAC_CONNEXION);

		// THEN: ListView is updated
		assertThat(connexionsList.getItems()).isEmpty();
	}

}
