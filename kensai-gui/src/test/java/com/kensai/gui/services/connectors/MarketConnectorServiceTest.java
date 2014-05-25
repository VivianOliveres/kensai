package com.kensai.gui.services.connectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

import java.util.concurrent.ExecutorService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.junit.Before;
import org.junit.Test;

import com.kensai.gui.services.ApplicationContext;
import com.kensai.gui.services.model.market.MarketConnectionModel;
import com.kensai.gui.services.task.TaskService;

public class MarketConnectorServiceTest {

	private static final MarketConnectionModel SWX = new MarketConnectionModel("SWX", "localhost", 1664, true);
	private static final MarketConnectionModel MIB = new MarketConnectionModel("MIB", "192.168.0.255", 4661, false);

	private MarketConnectorService service;

	private ObservableList<MarketConnectionModel> connections = FXCollections.observableArrayList(SWX, MIB);

	private ApplicationContext context = mock(ApplicationContext.class);
	private ExecutorService coreExecutor = mock(ExecutorService.class);
	private ExecutorService ioExecutor = mock(ExecutorService.class);

	@Before
	public void init() {
		TaskService taskService = mock(TaskService.class);
		given(taskService.getNettyCoreExecutor()).willReturn(coreExecutor);
		given(taskService.getNettyIOExecutor()).willReturn(ioExecutor);
		given(context.getTaskService()).willReturn(taskService);

		GuiChannelPipelineFactory pipelineFactory = mock(GuiChannelPipelineFactory.class);
		PipelineFactoryService pipelineFactoryService = mock(PipelineFactoryService.class);
		given(pipelineFactoryService.createPipelineFactory(any(MarketConnector.class))).willReturn(pipelineFactory);
		given(context.getPipelineFactoryService()).willReturn(pipelineFactoryService);

		service = new MarketConnectorService(connections, context);
	}

	@Test
	public void should_be_initialized_with_model() {
		// THEN: initialized with size of 2
		assertThat(service.getConnectors()).hasSize(2).contains(new MarketConnector(SWX, context), new MarketConnector(MIB, context));
	}

	@Test
	public void should_add_a_connector_when_model_add_a_connexion() {
		// GIVEN: New connection model
		MarketConnectionModel eurex = new MarketConnectionModel("EUREX", "192.168.0.255", 1234, false);

		// WHEN: add it to model connections
		connections.add(eurex);

		// THEN: a new connector is added
		assertThat(service.getConnectors()).hasSize(3).contains(new MarketConnector(eurex, context));
	}

	@Test
	public void should_remove_a_connector_when_model_remove_a_connexion() {
		// WHEN: remove SWX to model connections
		connections.remove(SWX);

		// THEN: a new connector is added
		assertThat(service.getConnectors()).hasSize(1).containsOnly(new MarketConnector(MIB, context));
	}

}
