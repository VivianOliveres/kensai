package com.kensai.gui.services.task;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import javafx.application.Platform;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class TaskService {
	private static Logger log = LogManager.getLogger(TaskService.class);

	private static final ThreadFactory BACKGROUND_THREAD_FACTORY = new ThreadFactoryBuilder()
																		.setNameFormat("BackGround")
																		.setUncaughtExceptionHandler((Thread t, Throwable e) -> log.error("Error in thread [" + t.getName() + "]", e))
																		.build();
	
	private static final ThreadFactory NETTY_CORE_THREAD_FACTORY = new ThreadFactoryBuilder()
																		.setNameFormat("Netty-Core")
																		.setUncaughtExceptionHandler((Thread t, Throwable e) -> log.error("Error in thread [" + t.getName() + "]", e))
																		.build();
	
	private static final ThreadFactory NETTY_IO_THREAD_FACTORY = new ThreadFactoryBuilder()
																		.setNameFormat("Netty-IO")
																		.setUncaughtExceptionHandler((Thread t, Throwable e) -> log.error("Error in thread [" + t.getName() + "]", e))
																		.build();

	private final ExecutorService backgroundExecutor;
	private final ExecutorService nettyCoreExecutor;
	private final ExecutorService nettyIOExecutor;

	public TaskService() {
		this(newSingleThreadExecutor(BACKGROUND_THREAD_FACTORY), 
			  newSingleThreadExecutor(NETTY_CORE_THREAD_FACTORY), 
			  newSingleThreadExecutor(NETTY_IO_THREAD_FACTORY));
	}

	public TaskService(ExecutorService backgroundExecutor, ExecutorService nettyCoreExecutor, ExecutorService nettyIOExecutor) {
		this.backgroundExecutor = backgroundExecutor;
		this.nettyCoreExecutor = nettyCoreExecutor;
		this.nettyIOExecutor = nettyIOExecutor;
	}

	public void runInBackground(Runnable command) {
		backgroundExecutor.execute(command);
	}

	public void runInGui(Runnable command) {
		Platform.runLater(command);
	}

	public ExecutorService getNettyCoreExecutor() {
		return nettyCoreExecutor;
	}

	public ExecutorService getNettyIOExecutor() {
		return nettyIOExecutor;
	}

	public ExecutorService getBackgroundExecutor() {
		return backgroundExecutor;
	}

}
