package com.kensai.gui.services.configuration.market;

import static com.kensai.gui.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.kensai.gui.services.task.TaskService;
import com.kensai.gui.xml.MarketConnectionDescriptor;
import com.kensai.gui.xml.MarketConnexionDescriptors;

@RunWith(MockitoJUnitRunner.class)
public class TestMarketConnexionConfigurationService {
	
	private MarketConnectionDescriptor connexionSWX = new MarketConnectionDescriptor("SWX", "localhost", 1664, true);
	private MarketConnectionDescriptor connexionMIB = new MarketConnectionDescriptor("MIB", "192.168.0.255", 4661, false);
	private MarketConnexionDescriptors connexions = new MarketConnexionDescriptors(connexionSWX, connexionMIB);

	private MarketConnectionConfigurationService service;

	@Mock private TaskService taskService;

	@Rule public TemporaryFolder configurationFolder = new TemporaryFolder();
	private File configurationFile;

	@Before
	public void init() throws IOException {
		configurationFile = configurationFolder.newFile("connections.xml");
		service = new MarketConnectionConfigurationService(taskService, configurationFile);
	}
	
	@Test
	public void should_save_and_read_connexions_from_file() {
		// GIVEN: 

		// WHEN: create new 
		service.setConnexions(connexions);

		// THEN: taskService is called
		verify(taskService).runInBackground(any(Runnable.class));
		
		// WHEN: save in file (should be done by TaskService)
		service.save();
		
		//THEN: File should contains connexions
		service = new MarketConnectionConfigurationService(taskService, configurationFile);
		assertThat(service.getConnexions()).hasConnexions(connexions.getConnexions().toArray(new MarketConnectionDescriptor[] {}));
	}
}
