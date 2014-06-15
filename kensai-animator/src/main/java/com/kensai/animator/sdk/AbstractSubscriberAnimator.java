package com.kensai.animator.sdk;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kensai.animator.core.MessageSender;
import com.kensai.protocol.Trading.CommandStatus;
import com.kensai.protocol.Trading.SubscribeCommand;
import com.kensai.protocol.Trading.User;

public class AbstractSubscriberAnimator extends AbstractAnimator {
	private static final Logger log = LogManager.getLogger(AbstractSubscriberAnimator.class);

	private static final int DEFAULT_SUBSCRIPTION_TIME = 5000;

	private Timer subscriptionTimer = new Timer("Subscribe-Timer", true);

	private MessageSender sender;

	private final User user;

	public AbstractSubscriberAnimator(User user) {
		this.user = user;
	}

	public User getUser() {
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
			log.error("Receive NAK when subscribing");
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
