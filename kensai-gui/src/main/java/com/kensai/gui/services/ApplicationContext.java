package com.kensai.gui.services;

import com.kensai.gui.services.configuration.ConfigurationService;
import com.kensai.gui.services.model.ModelService;
import com.kensai.gui.services.task.TaskService;


public class ApplicationContext {

	private GuiService guiService;
	private ConfigurationService confService;
	private ModelService modelService;
	private TaskService taskService;

	public ApplicationContext(TaskService taskService, ConfigurationService confService, ModelService modelService, GuiService guiService) {
		this.taskService = taskService;
		this.guiService = guiService;
		this.confService = confService;
		this.modelService = modelService;
	}

	public GuiService getGuiService() {
		return guiService;
	}

	public ConfigurationService getConfigurationService() {
		return confService;
	}

	public ModelService getModelService() {
		return modelService;
	}

	public TaskService getTaskService() {
		return taskService;
	}

}
