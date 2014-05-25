package com.kensai.gui;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import org.junit.BeforeClass;

public class AbstractTestJavaFX {

	@BeforeClass
	public static void initJavaFX() {
		new JFXPanel();
	}

	public static void runAndWait(final Runnable run) throws InterruptedException, ExecutionException {
		if (Platform.isFxApplicationThread()) {
			try {
				run.run();
			} catch (Exception e) {
				throw new ExecutionException(e);
			}

		} else {
			final Lock lock = new ReentrantLock();
			final Condition condition = lock.newCondition();
			final ThrowableWrapper throwableWrapper = new ThrowableWrapper();
			lock.lock();
			try {
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						lock.lock();
						try {
							run.run();
						} catch (Throwable e) {
							throwableWrapper.t = e;
						} finally {
							try {
								condition.signal();
							} finally {
								lock.unlock();
							}
						}
					}
				});
				condition.await();
				if (throwableWrapper.t != null) {
					throw new ExecutionException("Exception throwed: " + throwableWrapper.t.getMessage(), throwableWrapper.t);
				}
			} finally {
				lock.unlock();
			}
		}
	}

	private static class ThrowableWrapper {
		Throwable t;
	}
}
