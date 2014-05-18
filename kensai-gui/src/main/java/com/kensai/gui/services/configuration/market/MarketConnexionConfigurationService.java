package com.kensai.gui.services.configuration.market;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kensai.gui.services.task.TaskService;
import com.kensai.gui.xml.MarketConnexionDescriptor;
import com.kensai.gui.xml.MarketConnexionDescriptors;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class MarketConnexionConfigurationService {
	private static Logger log = LogManager.getLogger(MarketConnexionConfigurationService.class);

	private MarketConnexionDescriptors connexions;

	private final TaskService taskService;
	private final File configurationFile;

	private XStream xstream = new XStream(new StaxDriver());

	public MarketConnexionConfigurationService(TaskService taskService, Path configurationFile) {
		this(taskService, configurationFile.toFile());
	}

	public MarketConnexionConfigurationService(TaskService taskService, File configurationFile) {
		this.taskService = taskService;
		this.configurationFile = configurationFile;

		initXstrem();
		readConnexionsFromConfiguration();
	}

	private void initXstrem() {
		xstream.alias("MarketConnexionDescriptors", MarketConnexionDescriptors.class);
		xstream.alias("MarketConnexionDescriptor", MarketConnexionDescriptor.class);
	}

	private void readConnexionsFromConfiguration() {
		try {
			Reader isReader = new InputStreamReader(new FileInputStream((configurationFile)));
			connexions = (MarketConnexionDescriptors) xstream.fromXML(isReader);
			log.info("Read " + connexions.size() + " connexions");
		} catch (FileNotFoundException | StreamException e) {
			log.error("Can not read Market configuration from [" + configurationFile.getAbsolutePath() + "]", e);
			connexions = new MarketConnexionDescriptors();
		}
	}

	public void setConnexions(MarketConnexionDescriptors connexions) {
		this.connexions = connexions;
		taskService.runInBackground(() -> save());
	}

	protected void save() {
		try {
			Writer osWriter = new OutputStreamWriter(new FileOutputStream(configurationFile));
			PrettyPrintWriter ppWritter = new PrettyPrintWriter(osWriter);
			xstream.marshal(connexions, ppWritter);

		} catch (FileNotFoundException e) {
			log.error("Can not save Market configuration from [" + configurationFile.getAbsolutePath() + "]", e);
		}
	}

	public MarketConnexionDescriptors getConnexions() {
		return connexions;
	}

}
