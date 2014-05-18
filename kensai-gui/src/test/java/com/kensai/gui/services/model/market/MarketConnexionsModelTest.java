package com.kensai.gui.services.model.market;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;
import com.kensai.gui.services.configuration.market.MarketConnectionConfigurationService;
import com.kensai.gui.xml.MarketConnectionDescriptor;
import com.kensai.gui.xml.MarketConnexionDescriptors;

@RunWith(MockitoJUnitRunner.class)
public class MarketConnexionsModelTest {

	private static final MarketConnectionDescriptor SWX = new MarketConnectionDescriptor("SWX", "localhost", 1664, true);
	private static final MarketConnectionDescriptor MIB = new MarketConnectionDescriptor("MIB", "192.168.0.255", 4661, false);

	@Mock private MarketConnectionConfigurationService service;
	private List<MarketConnectionDescriptor> connectionsDescriptors;

	private MarketConnectionsModel connexions;

	@Before
	public void init() {
		connectionsDescriptors = Lists.newArrayList(SWX, MIB);
		MarketConnexionDescriptors descriptors = new MarketConnexionDescriptors(connectionsDescriptors);
		given(service.getConnexions()).willReturn(descriptors);
	}

	@Test
	public void should_initialize_with_service_ConnexionsDescriptors() {
		// WHEN: Create MarketConnexionsModel
		connexions = new MarketConnectionsModel(service);

		// THEN: Should contains SWX and MIB only
		assertThat(connexions.getConnexions()).hasSize(2);
	}

	@Test
	public void should_update_service_when_connexions_model_is_updated() {
		// GIVEN: Create MarketConnexionsModel
		connexions = new MarketConnectionsModel(service);

		// WHEN: update model
		MarketConnectionModel eurex = new MarketConnectionModel("EUREX", "localhost", 1234, true);
		connexions.getConnexions().add(eurex);

		// THEN: Should contains SWX, MIB and eurex only
		assertThat(connexions.getConnexions()).hasSize(3);

		// AND: update service
		verify(service).setConnexions(any(MarketConnexionDescriptors.class));
	}
}
