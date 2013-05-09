package fr.kensai.fmk.providing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import fr.kensai.fmk.providing.DataListener;
import fr.kensai.fmk.providing.DataProvider;
import fr.kensai.fmk.providing.DefaultDataProviderService;
import fr.kensai.fmk.providing.ProviderController;

public class TestDefaultDataProviderService {

	private DataProvider provider;

	private DataListener listener;

	private DefaultDataProviderService service;

	@Before
	public void init() {
		provider = mock(DataProvider.class);
		listener = mock(DataListener.class);

		service = new DefaultDataProviderService();
	}

	@Test
	public void should_create_controller_by_listening_providing() {
		// GIVEN: one provider is registered
		service.addProvider(provider);

		// WHEN: listen type
		ProviderController controller = service.addListener(listener);

		// THEN: controller has been created
		assertThat(controller).isNotNull();
		assertThat(controller.getComboBox()).isNotNull();
		assertThat(controller.getListener()).isNotNull().isEqualsToByComparingFields(listener);
	}

	@Test
	public void should_listener_be_registered_to_provider() {
		// GIVEN: one provider listening to Object.class is registered
		when(provider.getProvidenClass()).thenReturn(Object.class);
		service.addProvider(provider);

		// AND: listener is for Object.class
		List<Class> listenClasses = Lists.newArrayList();
		listenClasses.add(Object.class);
		when(listener.getListenClasses()).thenReturn(listenClasses);

		// WHEN: listen type and select listener
		ProviderController controller = service.addListener(listener);
		controller.getComboBox().getSelectionModel().select(provider);

		// THEN: provider is listening listener
		verify(provider).addListener(eq(listener));

		// WHEN: select empty provider
		controller.getComboBox().getSelectionModel().select(controller.getDefaultProvider());

		// THEN: provider do not listen listener
		verify(provider).removeListener(eq(listener));

		// AND: listener has received changes from EmptyDataProvider
		verify(listener).selectionChanged(anyList());
	}

	@Test
	public void should_adding_provider_be_registered_to_listener() {
		// GIVEN: listener for Object.class
		List<Class> listenClasses = Lists.newArrayList();
		listenClasses.add(Object.class);
		when(listener.getListenClasses()).thenReturn(listenClasses);

		// WHEN: listener is registered
		ProviderController controller = service.addListener(listener);

		// THEN: listener is registered to EmptyDataProvider
		assertThat(controller.getComboBox().getItems()).containsOnly(controller.getDefaultProvider());

		// GIVEN: one provider listening to Object.class
		when(provider.getProvidenClass()).thenReturn(Object.class);

		// WHEN: adding provider
		service.addProvider(provider);

		// THEN: controller contains now provider
		assertThat(controller.getComboBox().getItems()).containsOnly(controller.getDefaultProvider(), provider);

		// WHEN: selection has changed to new provider
		controller.getComboBox().getSelectionModel().select(provider);

		// THEN: provider is registered to listener
		verify(provider).addListener(eq(listener));
	}
}
