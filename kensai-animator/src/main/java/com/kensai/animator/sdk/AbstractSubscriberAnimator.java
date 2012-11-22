package com.kensai.animator.sdk;

import java.util.Timer;
import java.util.TimerTask;

import com.kensai.animator.core.MessageSender;
import com.kensai.protocol.Trading.SubscribeCommand;

public class AbstractSubscriberAnimator extends AbstractAnimator {

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
		SubscribeCommand cmd = SubscribeCommand.newBuilder().setUser(user).build();
		sender.send(cmd);
	}

	public MessageSender getMessageSender() {
		return sender;
	}

	@Override
	public void setMessageSender(MessageSender sender) {
		this.sender = sender;
	}
}
