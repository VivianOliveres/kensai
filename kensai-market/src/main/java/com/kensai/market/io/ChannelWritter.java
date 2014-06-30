package com.kensai.market.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

public class ChannelWritter {
	private static final Logger log = LogManager.getLogger(ChannelWritter.class);

	private final Channel channel;
	private final String user;

	public ChannelWritter(Channel channel, String user) {
		this.channel = channel;
		this.user = user;
	}

	public ChannelFuture write(Object msg) {
		if (!channel.isOpen()) {
			log.error("Can not write message to [{}] when channel is not open", user);
			return null;

		} else if (!channel.isConnected()) {
			log.error("Can not write message to [{}] when channel is not connected", user);
			return null;

		} else if (!channel.isWritable()) {
			log.error("Can not write message to [{}] when channel is not writable", user);
			return null;

		} else {
			log.debug("Send message to [{}]: {}", user, msg);
			return channel.write(msg);
		}
	}

	public Channel getChannel() {
		return channel;
	}
}
