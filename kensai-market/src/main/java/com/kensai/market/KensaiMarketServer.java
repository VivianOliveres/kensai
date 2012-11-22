package com.kensai.market;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kensai.market.core.InstrumentDepth;
import com.kensai.market.core.KensaiMarket;
import com.kensai.market.io.KensaiMessageSender;
import com.kensai.market.io.MarketServerChannelPipelineFactory;
import com.kensai.market.persist.InstrumentDepthsLoader;

public class KensaiMarketServer {

	private static final Logger log = LoggerFactory.getLogger(KensaiMarketServer.class);

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

		// Retrieve instruments and initial summaries
		URL url = KensaiMarketServer.class.getClassLoader().getResource("cac.csv");
		Path path = Paths.get(url.toURI());
		List<InstrumentDepth> depths;
		try {
			depths = new InstrumentDepthsLoader(path).load();

		} catch (Exception e) {
			log.error("Can not load instruments", e);
			return;
		}
		if (depths.isEmpty()) {
			throw new IllegalArgumentException("Dictionary is empty - load correct values before starting server!!!");
		}

		// Initialize core classes
		KensaiMessageSender sender = new KensaiMessageSender();
		KensaiMarket core = new KensaiMarket(sender, depths);

		// Initialize server
		ServerBootstrap bootstrap = new ServerBootstrap(factory);
		bootstrap.setPipelineFactory(new MarketServerChannelPipelineFactory(core));
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
		bootstrap.bind(new InetSocketAddress(SERVER_PORT));
	}

}
