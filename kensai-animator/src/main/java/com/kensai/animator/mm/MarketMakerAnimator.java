package com.kensai.animator.mm;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kensai.animator.core.MessageSender;
import com.kensai.animator.sdk.AbstractSubscriberAnimator;
import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.SummariesSnapshot;
import com.kensai.protocol.Trading.Summary;
import com.kensai.protocol.Trading.User;

public class MarketMakerAnimator extends AbstractSubscriberAnimator {
	private static final Logger log = LogManager.getLogger(MarketMakerAnimator.class);

	private final User user;
	private final int qty;
	private final double delta;

	private Map<Instrument, MarketMakerInstrumentAnimator> animators = newHashMap();

	public MarketMakerAnimator(User user, int qty, double delta) {
		super(user);
		this.user = user;
		this.qty = qty;
		this.delta = delta;
	}

	@Override
	public void onSnapshot(SummariesSnapshot snapshot) {
		// Check preconditions
		if (snapshot == null || snapshot.getSummariesCount() <= 0) {
			log.warn("Receive an invalid summary snapshot: {}", snapshot);
			return;
		}

		// Create InstrumentAnimator
		for (Summary summary : snapshot.getSummariesList()) {
			Instrument instrument = summary.getInstrument();
			// TODO: for debug purpose (only one instrument)
			// if (animators.containsKey(instrument) || !animators.isEmpty()) {
			if (animators.containsKey(instrument)) {
				continue;
			}

			MarketMakerInstrumentAnimator animator = new MarketMakerInstrumentAnimator(user, qty, delta, instrument, getMessageSender());
			animator.setMessageSender(getMessageSender());
			animators.put(instrument, animator);
			animator.onSummary(summary);
		}
	}

	@Override
	public void onSummary(Summary summary) {
		log.debug("onSummary({})", summary);

		// Retrieve InstrumentAnimator (or create it)
		Instrument instrument = summary.getInstrument();
		MarketMakerInstrumentAnimator animator;
		if (animators.containsKey(instrument)) {
			animator = animators.get(instrument);

		} else {
			return;
		}

		// Redirect call on it
		animator.onSummary(summary);
	}

	@Override
	public void setMessageSender(MessageSender sender) {
		super.setMessageSender(sender);

		// Send subscribe
		subscribe();
	}
}
