package fr.kensai.fmk.providing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class TestProviderController {

	private DataProvider provider;
	private DataListener listener;

	private ProviderController controller;

	@Before
	public void init() {
		// GIVEN: provider and listener
		provider = mock(DataProvider.class);
		listener = mock(DataListener.class);

		// WHEN: controller is created
		controller = new ProviderController(listener, Arrays.asList(provider));

		// THEN: combo is initialized
		assertThat(controller.getComboBox()).isNotNull();
		assertThat(controller.getComboBox().getItems()).isNotNull().containsOnly(provider, new EmptyDataProvider());
	}

	@Test
	public void should_addProvider_update_combobox_list() {
		// WHEN: add listener
		DataProvider newProvider = mock(DataProvider.class);
		controller.addProvider(newProvider);

		// THEN: one item is add in combo
		assertThat(controller.getComboBox().getItems()).isNotNull().containsOnly(provider, newProvider, new EmptyDataProvider());
	}

	@Test
	public void should_removeProvider_update_combobox_list() {
		// WHEN: remove listener
		controller.removeProvider(provider);

		// THEN: no more items are in combo
		assertThat(controller.getComboBox().getItems()).isNotNull().containsOnly(new EmptyDataProvider());
	}
}
