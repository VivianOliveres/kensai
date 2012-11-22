package com.kensai.animator.random;

import com.kensai.animator.core.MessageSender;
import com.kensai.animator.sdk.AbstractAnimator;
import com.kensai.protocol.Trading.SummariesSnapshot;
import com.kensai.protocol.Trading.Summary;

public class RandomInstrumentAnimator extends AbstractAnimator {

	private final String user;
	private final int minQty;
	private final int maxQty;

	private MessageSender sender;

	public RandomInstrumentAnimator(String user, int minQty, int maxQty, MessageSender sender) {
		this.user = user;
		this.minQty = minQty;
		this.maxQty = maxQty;
		this.sender = sender;
	}

	@Override
	public void onSnapshot(SummariesSnapshot snapshot) {
		if (snapshot == null || snapshot.getSummariesCount() <= 0) {
			return;
		}

		for (Summary summary : snapshot.getSummariesList()) {
			onSummary(summary);
		}
	}

	@Override
	public void onSummary(Summary summary) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMessageSender(MessageSender sender) {
		this.sender = sender;
	}

}
