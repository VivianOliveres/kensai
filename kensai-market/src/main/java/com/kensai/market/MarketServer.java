package com.kensai.market;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class MarketServer {

	private static final Logger log = LoggerFactory.getLogger(MarketServer.class);

	private static final int SERVER_PORT = 1664;

	public static void main(String[] args) throws Exception {
		UncaughtExceptionHandler exceptionHandler = new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				log.error("Uncaught exception in thread [{}]", t == null ? null : t.getName(), e);
			}
		};

		// Set DefaultUncaughtExceptionHandler for all threads
		Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);

		// Create ChannelFactory which uses only one thread for ChannelHandler
		ThreadFactory coreThreadFactory = new ThreadFactoryBuilder().setDaemon(false).setNameFormat("CoreThread")
			.setUncaughtExceptionHandler(exceptionHandler).build();
		ExecutorService coreExecutor = Executors.newSingleThreadExecutor(coreThreadFactory);
		ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), coreExecutor);

		ServerBootstrap bootstrap = new ServerBootstrap(factory);
		bootstrap.setPipelineFactory(new MarketServerChannelPipelineFactory());
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
		bootstrap.bind(new InetSocketAddress(SERVER_PORT));
	}

}
