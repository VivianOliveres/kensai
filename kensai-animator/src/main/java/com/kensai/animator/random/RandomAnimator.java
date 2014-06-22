package com.kensai.animator.random;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kensai.animator.core.MessageSender;
import com.kensai.animator.sdk.AbstractSubscriberAnimator;
import com.kensai.animator.sdk.UserDataGenerator;
import com.kensai.protocol.Trading.BuySell;
import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.OrderAction;
import com.kensai.protocol.Trading.SummariesSnapshot;
import com.kensai.protocol.Trading.Summary;
import com.kensai.protocol.Trading.User;

public class RandomAnimator extends AbstractSubscriberAnimator {

	private static final Logger log = LogManager.getLogger(RandomAnimator.class);

	private final Random random = new Random();

	private final User user;
	private final int minQty;
	private final int maxQty;

	private Map<Instrument, Summary> instruments = newHashMap();

	private final Timer timer;

	public RandomAnimator(User user, int minQty, int maxQty, int delay, int period) {
		super(user);
		this.user = user;
		this.minQty = minQty;
		this.maxQty = maxQty;

		TimerTask task = createTask();
		timer = new Timer("Random[" + user.getName() + "]", true);
		timer.schedule(task, delay, period);
	}

	private TimerTask createTask() {
		return new TimerTask() {

			@Override
			public void run() {
				synchronized (instruments) {
					if (instruments.isEmpty()) {
						return;
					}

					log.info("Run task on {} instruments", instruments.size());
					for (Summary summary : instruments.values()) {
						boolean isBuyOrder = random.nextBoolean();
						int qty = generateQty(summary);
						double price = generatePrice(summary);
						sendOrder(qty, price, isBuyOrder ? BuySell.BUY : BuySell.SELL, summary.getInstrument());
					}
				}
			}
		};
	}

	private int generateQty(Summary summary) {
		int qty = random.nextInt(maxQty - minQty);
		return qty + minQty + 1;
	}

	private double generatePrice(Summary summary) {
		boolean isBuyDepthEmpty = summary.getBuyDepthsList().isEmpty();
		boolean isSellDepthEmpty = summary.getSellDepthsList().isEmpty();
		if (isBuyDepthEmpty || isSellDepthEmpty) {
			return summary.getLast();
		}

		double lowPrice = summary.getBuyDepths(summary.getBuyDepthsCount() - 1).getPrice() - 0.5;
		double highPrice = summary.getSellDepths(summary.getSellDepthsCount() - 1).getPrice() + 0.5;
		double factor = random.nextDouble();
		return round(lowPrice + factor * highPrice);
	}

	private void sendOrder(int qty, double price, BuySell side, Instrument instrument) {
		String userData = UserDataGenerator.generate();
		log.info("send order [instr[{}] side[{}] qty[{}] price[{}] userData[{}] user[{}]]", new Object[] { instrument.getName(), side, qty, price,
				userData, user.getName() });
		Order order = Order.newBuilder().setAction(OrderAction.INSERT).setInitialQuantity(qty).setInstrument(instrument).setPrice(price).setSide(side)
			.setUserData(userData).setUser(user).build();
		getMessageSender().send(order);
	}

	@Override
	public void onSnapshot(SummariesSnapshot snapshot) {
		// Check preconditions
		if (snapshot == null || snapshot.getSummariesCount() <= 0) {
			log.error("Receive an invalid summary snapshot: {}", snapshot);
			return;
		}

		// Create InstrumentAnimator
		synchronized (instruments) {
			instruments.clear();
			for (Summary summary : snapshot.getSummariesList()) {
				instruments.put(summary.getInstrument(), summary);

				// Initialize with orders
				int qty = generateQty(summary);
				double price = summary.getLast();
				sendOrder(qty, price, BuySell.BUY, summary.getInstrument());
				sendOrder(qty, round(price + 0.1), BuySell.SELL, summary.getInstrument());
			}
		}
	}

	@Override
	public void onSummary(Summary summary) {
		synchronized (instruments) {
			Instrument instrument = summary.getInstrument();
			instruments.put(instrument, summary);
		}
	}

	@Override
	public void setMessageSender(MessageSender sender) {
		super.setMessageSender(sender);

		// Send subscribe
		subscribe();
	}

}
