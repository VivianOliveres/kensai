package com.kensai.gui.services.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class TaskService {
	private static Logger log = LogManager.getLogger(TaskService.class);

	private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder()
																	.setNameFormat("BackGround")
																	.setUncaughtExceptionHandler((Thread t, Throwable e) -> log.error("Error in thread [" + t.getName() + "]", e))
																	.build();

	private ExecutorService backgroundExecutor;

	public TaskService() {
		this(Executors.newSingleThreadExecutor(THREAD_FACTORY));
	}

	public TaskService(ExecutorService backgroundExecutor) {
		this.backgroundExecutor = backgroundExecutor;
	}

	public void runInBackground(Runnable command) {
		backgroundExecutor.execute(command);
	}

}
