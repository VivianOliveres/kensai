package fr.kensai.fmk.docking;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import org.junit.Before;
import org.junit.Test;

import fr.kensai.fmk.providing.DataListener;
import fr.kensai.fmk.providing.DataProvider;
import fr.kensai.fmk.providing.DataProviderService;
import fr.kensai.fmk.providing.ProviderController;
import fr.kensai.fmk.view.View;

public class TestDecoratedView {

	private static final String VIEW_NAME = "VIEW_NAME";

	private DataProviderService service;
	private ProviderController controller;
	private ComboBox<DataProvider> combo;

	private View view;
	private Node node;
	private DataListener listener;
	private DataProvider provider;

	private DecoratedView decorated;

	@Before
	public void init() {

		view = mock(View.class);
		when(view.getViewName()).thenReturn(VIEW_NAME);

		node = new Label("coucou");
		when(view.getComponent()).thenReturn(node);

		listener = mock(DataListener.class);
		when(view.getDataListener()).thenReturn(listener);

		provider = mock(DataProvider.class);
		List<DataProvider> providers = newArrayList(provider);
		when(view.getDataProviders()).thenReturn(providers);

		combo = new ComboBox<>();

		controller = mock(ProviderController.class);
		when(controller.getComboBox()).thenReturn(combo);

		service = mock(DataProviderService.class);
		when(service.addListener(eq(listener))).thenReturn(controller);
	}

	@Test
	public void should_build_decorated_view() {
		// WHEN: create DecoratedView
		decorated = new DecoratedView(view, service);

		// THEN: Decorated view is created
		assertThat(decorated.getText()).isEqualTo(VIEW_NAME);
		assertThat(decorated.getContent()).isInstanceOf(BorderPane.class);
		BorderPane content = (BorderPane) decorated.getContent();

		// AND: CenterPane contains Node
		assertThat(content.getCenter()).isEqualTo(node);

		// AND: header contains combo
		assertThat(content.getTop()).isInstanceOf(Pane.class);
		Pane top = (Pane) content.getTop();
		assertThat(top.getChildren()).contains(combo);

		// AND: DataListener has been registered to service
		verify(service).addListener(eq(listener));

		// AND: provider has been registered to service
		verify(service).addProvider(eq(provider));
	}

}
