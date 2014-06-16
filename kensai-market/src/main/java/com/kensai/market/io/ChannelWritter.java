package com.kensai.market.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

public class ChannelWritter {
	private static final Logger log = LogManager.getLogger(ChannelWritter.class);

	private final Channel channel;

	public ChannelWritter(Channel channel) {
		this.channel = channel;
	}

	public ChannelFuture write(Object msg) {
		if (!channel.isOpen()) {
			log.error("Can not write message when channel is not open");
			return null;

		} else if (!channel.isConnected()) {
			log.error("Can not write message when channel is not connected");
			return null;

		} else if (!channel.isWritable()) {
			log.error("Can not write message when channel is not writable");
			return null;

		} else {
			return channel.write(msg);
		}
	}

	public Channel getChannel() {
		return channel;
	}
}
