package com.kensai.gui.services;

import com.kensai.gui.services.model.ModelService;
import com.kensai.gui.services.task.TaskService;
import com.kensai.gui.views.MarketConnectionsViewController;

public class GuiService {

	private ModelService modelService;
	private TaskService taskService;

	private MarketConnectionsViewController marketConnectionsViewController;

	public GuiService(ModelService modelService, TaskService taskService) {
		this.modelService = modelService;
		this.taskService = taskService;

		initViews();
	}

	private void initViews() {
		marketConnectionsViewController = new MarketConnectionsViewController(modelService, taskService);
	}

	public MarketConnectionsViewController getMarketConnectionsViewController() {
		return marketConnectionsViewController;
	}

}
