package com.kensai.animator.mm;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kensai.animator.core.MessageSender;
import com.kensai.animator.sdk.AbstractAnimator;
import com.kensai.animator.sdk.UserDataGenerator;
import com.kensai.protocol.Trading.BuySell;
import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.OrderAction;
import com.kensai.protocol.Trading.SummariesSnapshot;
import com.kensai.protocol.Trading.Summary;
import com.kensai.protocol.Trading.User;

public class MarketMakerInstrumentAnimator extends AbstractAnimator {
	private static final Logger log = LogManager.getLogger(MarketMakerInstrumentAnimator.class);

	private final Random random = new Random();

	private final User user;
	private final int qty;
	private final double delta;

	private MessageSender sender;

	private final Instrument instrument;

	public MarketMakerInstrumentAnimator(User user, int qty, double delta, Instrument instrument, MessageSender sender) {
		this.user = user;
		this.qty = qty;
		this.delta = delta;
		this.instrument = instrument;
		this.sender = sender;
	}

	@Override
	public void onSnapshot(SummariesSnapshot snapshot) {
		if (snapshot == null || snapshot.getSummariesCount() <= 0) {
			return;
		}

		for (Summary summary : snapshot.getSummariesList()) {
			if (summary.getInstrument().equals(instrument)) {
				onSummary(summary);
			}
		}
	}

	@Override
	public void onSummary(Summary summary) {
		// Precondition
		if (summary == null) {
			log.error("Receive an invalid summary [{}]", summary);
			return;
		}

		log.debug("onSummary({})", summary);
		boolean isBuyDepthEmpty = summary.getBuyDepthsList().isEmpty();
		boolean isSellDepthEmpty = summary.getSellDepthsList().isEmpty();
		if (isBuyDepthEmpty || isSellDepthEmpty) {
			initializeDepth(isBuyDepthEmpty, isSellDepthEmpty, summary);
			return;
		}

		// Check prices
		double buyPrice = summary.getBuyDepths(0).getPrice();
		double sellPrice = summary.getSellDepths(0).getPrice();
		log.info("sellPrice[{}] buyPrice[{}] delta[{}]", sellPrice, buyPrice, delta);
		if (sellPrice - buyPrice > delta) {
			log.debug(summary.toString());
			boolean isBuyOrder = random.nextBoolean();
			double price = isBuyOrder ? sellPrice - delta - 0.1 : buyPrice + delta + 0.1;
			sendOrder(qty, price, isBuyOrder ? BuySell.BUY : BuySell.SELL, UserDataGenerator.generate());
			return;
		}

		// Check BuyQty
		int buyQty = summary.getBuyDepths(0).getQuantity();
		if (buyQty < qty) {
			int addedQty = qty - buyQty;
			log.info("buyQty[{}] < qty[{}] -> send order", buyQty, qty);
			log.debug(summary.getBuyDepthsList().toString());
			sendOrder(addedQty, buyPrice, BuySell.BUY, UserDataGenerator.generate());
		}

		// Check SellQty
		int sellQty = summary.getSellDepths(0).getQuantity();
		if (sellQty < qty) {
			int addedQty = qty - sellQty;
			log.info("sellQty[{}] < qty[{}] -> send order", sellQty, qty);
			log.debug(summary.getSellDepthsList().toString());
			sendOrder(addedQty, sellPrice, BuySell.SELL, UserDataGenerator.generate());
		}
	}

	private void initializeDepth(boolean isBuyDepthEmpty, boolean isSellDepthEmpty, Summary summary) {
		if (isBuyDepthEmpty && isSellDepthEmpty) {
			// Send random order -> at reception of summary, it will send another order
			BuySell side = random.nextBoolean() ? BuySell.BUY : BuySell.SELL;
			sendOrder(qty, summary.getLast(), side, UserDataGenerator.generate());

		} else if (isBuyDepthEmpty) {
			double price = summary.getSellDepths(0).getPrice() - delta;
			sendOrder(qty, price, BuySell.BUY, UserDataGenerator.generate());

		} else if (isSellDepthEmpty) {
			double price = summary.getBuyDepths(0).getPrice() + delta;
			sendOrder(qty, price, BuySell.SELL, UserDataGenerator.generate());
		}
	}

	private void sendOrder(int initialQty, double price, BuySell side, String userData) {
		double roundedPrice = round(price);
		log.info("send order [instr[{}] qty[{}] price[{}] side[{}] userData[{}] user[{}]]",  instrument.getName(), qty, roundedPrice, side, userData, user.getName());
		Order order = Order.newBuilder()
								 .setInstrument(instrument)
								 .setAction(OrderAction.INSERT)
								 .setInitialQuantity(initialQty)
								 .setPrice(roundedPrice)
								 .setUser(user)
								 .setSide(side)
								 .setUserData(userData)
								 .build();
		sender.send(order);
	}

	@Override
	public void setMessageSender(MessageSender sender) {
		this.sender = sender;
	}

}
