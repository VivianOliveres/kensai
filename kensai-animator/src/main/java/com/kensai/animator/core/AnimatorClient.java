package com.kensai.animator.core;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kensai.animator.sdk.Animator;

public class AnimatorClient {

	private static final Logger log = LogManager.getLogger(AnimatorClient.class);

	private static final UncaughtExceptionHandler exceptionHandler = new UncaughtExceptionHandler() {

		@Override
		public void uncaughtException(Thread t, Throwable e) {
			log.error("Uncaught exception in thread [{}]", t == null ? null : t.getName(), e);
		}
	};

	private static final ThreadFactory coreThreadFactory = new ThreadFactoryBuilder().setDaemon(false).setNameFormat("AnimatorCore")
		.setUncaughtExceptionHandler(exceptionHandler).build();

	private final String host;
	private final int port;
	private final Animator animator;

	private ExecutorService coreExecutor;

	public AnimatorClient(String host, int port, Animator animator) {
		this(host, port, animator, Executors.newSingleThreadExecutor(coreThreadFactory));
	}

	public AnimatorClient(String host, int port, Animator animator, ExecutorService coreExecutor) {
		this.host = host;
		this.port = port;
		this.animator = animator;
		this.coreExecutor = coreExecutor;
	}

	public void run() {
		// Create ChannelFactory which uses only one thread for ChannelHandler
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), coreExecutor));

		// Configure the event pipeline factory.
		bootstrap.setPipelineFactory(new AnimatorChannelPipelineFactory(animator));
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);

		// Make a new connection.
		ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(host, port));

		// Wait until the connection is made successfully.
		connectFuture.awaitUninterruptibly();
		if (!connectFuture.isSuccess()) {
			log.error("Can not connect to server [" + host + ":" + port + "]", connectFuture.getCause());
			System.exit(0);
		}
	}
}
