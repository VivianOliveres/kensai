package com.kensai.gui.services.connectors;

import java.util.concurrent.ExecutorService;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.kensai.gui.services.ApplicationContext;

public class ClientBoostrapFactory {

	public static ClientBootstrap create(ApplicationContext context, MarketConnector connector) {
		return create(context.getTaskService().getNettyIOExecutor(), 
						  context.getTaskService().getNettyCoreExecutor(), 
						  context.getPipelineFactoryService().createPipelineFactory(connector));
	}

	public static ClientBootstrap create(ExecutorService ioExecutor, ExecutorService coreService, GuiChannelPipelineFactory pipelineFactory) {
		NioClientSocketChannelFactory channelFactory = new NioClientSocketChannelFactory(ioExecutor, coreService);
		ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);
		bootstrap.setPipelineFactory(pipelineFactory);
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
		return bootstrap;
	}
}
