package com.kensai.animator.mm;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kensai.animator.core.MessageSender;
import com.kensai.animator.sdk.AbstractSubscriberAnimator;
import com.kensai.protocol.Trading.CommandStatus;
import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.SubscribeCommand;
import com.kensai.protocol.Trading.SummariesSnapshot;
import com.kensai.protocol.Trading.Summary;

public class MarketMakerAnimator extends AbstractSubscriberAnimator {

	private static final Logger log = LoggerFactory.getLogger(MarketMakerAnimator.class);

	private final String user;
	private final int minQty;
	private final int maxQty;

	private Map<Instrument, MarketMakerInstrumentAnimator> animators = newHashMap();

	public MarketMakerAnimator(String user, int minQty, int maxQty) {
		super(user);
		this.user = user;
		this.minQty = minQty;
		this.maxQty = maxQty;
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
			if (animators.containsKey(instrument)) {
				continue;
			}

			MarketMakerInstrumentAnimator animator = new MarketMakerInstrumentAnimator(user, minQty, maxQty, instrument, getMessageSender());
			animators.put(instrument, animator);
			animator.onSummary(summary);
		}
	}

	@Override
	public void onSummary(Summary summary) {
		// Retrieve InstrumentAnimator (or create it)
		Instrument instrument = summary.getInstrument();
		MarketMakerInstrumentAnimator animator;
		if (animators.containsKey(instrument)) {
			animator = animators.get(instrument);

		} else {
			animator = new MarketMakerInstrumentAnimator(user, minQty, maxQty, instrument, getMessageSender());
			animators.put(instrument, animator);
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
