package com.kensai.animator.sdk;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kensai.animator.core.MessageSender;
import com.kensai.protocol.Trading.CommandStatus;
import com.kensai.protocol.Trading.SubscribeCommand;

public class AbstractSubscriberAnimator extends AbstractAnimator {
	private static final Logger log = LogManager.getLogger(AbstractSubscriberAnimator.class);

	private static final int DEFAULT_SUBSCRIPTION_TIME = 5000;

	private Timer subscriptionTimer = new Timer("Subscribe-Timer", true);

	private MessageSender sender;

	private final String user;

	public AbstractSubscriberAnimator(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	protected void requestSubscribe() {
		requestSubscribe(DEFAULT_SUBSCRIPTION_TIME);
	}

	protected void requestSubscribe(int delay) {
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				subscribe();
			}
		};
		subscriptionTimer.schedule(task, delay);
	}

	protected void subscribe() {
		log.info("Send subscribe command");
		SubscribeCommand cmd = SubscribeCommand.newBuilder().setUser(user).build();
		sender.send(cmd);
	}

	@Override
	public void onSubscribe(SubscribeCommand cmd) {
		log.debug("onSubscribe({})", cmd);
		if (cmd == null) {
			log.warn("Receive an invalid SubscribeCommand: {}", cmd);
			return;
		}

		if (cmd.getStatus().equals(CommandStatus.ACK)) {
			log.info("Successfully connected to market!");

		} else {
			log.error("Can not subscribe to market. Schedule a retry...");
			requestSubscribe();
		}
	}

	public MessageSender getMessageSender() {
		return sender;
	}

	@Override
	public void setMessageSender(MessageSender sender) {
		this.sender = sender;
	}
}
