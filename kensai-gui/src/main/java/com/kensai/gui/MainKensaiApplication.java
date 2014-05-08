package com.kensai.gui;

import java.io.File;
import java.net.URL;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kensai.gui.services.ApplicationContext;
import com.kensai.gui.services.GuiService;
import com.kensai.gui.services.configuration.ConfigurationService;
import com.kensai.gui.services.configuration.market.MarketConnexionConfigurationService;
import com.kensai.gui.services.model.ModelService;
import com.kensai.gui.services.task.TaskService;
import com.kensai.gui.views.MarketConnectionsViewController;

public class MainKensaiApplication extends Application {
	private static Logger log = LogManager.getLogger(MainKensaiApplication.class);

	@Override
	public void start(Stage stage) throws Exception {
		log.info("Start application");

		// Create context
		ApplicationContext context = createApplicationContext();

		// Create GuiService
		GuiService initializer = createGuiService(context);

		// Init stage
		Scene scene = createScene(context, initializer);
		scene.getStylesheets().add("style-dark.css");
		stage.setScene(scene);
		stage.setTitle("Kensai Assurance Vie");
		stage.show();
		log.info("Application started");
	}

	private ApplicationContext createApplicationContext() {
		TaskService taskService = new TaskService();

		URL resource = getClass().getClassLoader().getResource("datas/connexions.xml");
		File marketConfFile = new File(resource.getFile());
		log.info("createApplicationContext - create MarketConnexionConfigurationService from: " + marketConfFile);
		if (!marketConfFile.exists()) {
			throw new RuntimeException("Can not find ressource: " + resource);
		}
		MarketConnexionConfigurationService marketConnexionConfigurationService = new MarketConnexionConfigurationService(taskService, marketConfFile);

		ConfigurationService confService = new ConfigurationService(marketConnexionConfigurationService);
		ModelService modelService = new ModelService(marketConnexionConfigurationService.getConnexions());

		ApplicationContext context = new ApplicationContext(taskService, confService, modelService);
		return context;
	}

	private GuiService createGuiService(ApplicationContext context) {
		return new GuiService(context);
	}

	private Scene createScene(ApplicationContext context, GuiService guiService) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(10));

		MarketConnectionsViewController marketConnectionsViewController = guiService.getMarketConnectionsViewController();
		root.setLeft(marketConnectionsViewController.getView());

		return new Scene(root);
	}

	public static void main(String[] args) {
		launch(args);
	}

}
