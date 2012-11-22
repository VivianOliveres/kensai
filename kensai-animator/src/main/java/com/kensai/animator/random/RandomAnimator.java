package com.kensai.animator.random;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kensai.animator.core.MessageSender;
import com.kensai.animator.sdk.AbstractSubscriberAnimator;
import com.kensai.animator.sdk.UserDataGenerator;
import com.kensai.protocol.Trading.BuySell;
import com.kensai.protocol.Trading.CommandStatus;
import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.OrderAction;
import com.kensai.protocol.Trading.SubscribeCommand;
import com.kensai.protocol.Trading.SummariesSnapshot;
import com.kensai.protocol.Trading.Summary;

public class RandomAnimator extends AbstractSubscriberAnimator {

	private static final Logger log = LoggerFactory.getLogger(RandomAnimator.class);

	private final Random random = new Random();

	private final String user;
	private final int minQty;
	private final int maxQty;

	private Map<Instrument, Summary> instruments = newHashMap();

	private final Timer timer;

	public RandomAnimator(String user, int minQty, int maxQty, int delay, int period) {
		super(user);
		this.user = user;
		this.minQty = minQty;
		this.maxQty = maxQty;

		TimerTask task = createTask();
		timer = new Timer("Random[" + user + "]", true);
		timer.schedule(task, delay, period);
	}

	private TimerTask createTask() {
		return new TimerTask() {

			@Override
			public void run() {
				synchronized (instruments) {
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
		boolean isBuyDeothEmpty = summary.getBuyDepthsList().isEmpty();
		boolean isSellDeothEmpty = summary.getSellDepthsList().isEmpty();
		if (isBuyDeothEmpty || isSellDeothEmpty) {
			return summary.getLast();
		}

		double lowPrice = summary.getBuyDepths(summary.getBuyDepthsCount() - 1).getPrice() - 0.5;
		double highPrice = summary.getSellDepths(summary.getSellDepthsCount() - 1).getPrice() + 0.5;
		double factor = random.nextDouble();
		return lowPrice + round(factor * highPrice);
	}

	private double round(double value) {
		double floor = Math.floor(value);
		double rest = value - floor;
		if (rest < 0.5) {
			return floor;

		} else {
			return floor + 0.5;
		}
	}

	private void sendOrder(int qty, double price, BuySell side, Instrument instrument) {
		String userData = UserDataGenerator.generate();
		log.info("send order [instr[{}] side[{}] qty[{}] price[{}] userData[{}] user[{}]]", new Object[] { instrument.getName(), side, qty, price,
				userData, user });
		Order order = Order.newBuilder().setAction(OrderAction.INSERT).setInitialQuantity(qty).setInstrument(instrument).setPrice(price).setSide(side)
			.setUserData(userData).setUser(user).build();
		getMessageSender().send(order);
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
		synchronized (instruments) {
			instruments.clear();
			for (Summary summary : snapshot.getSummariesList()) {
				instruments.put(summary.getInstrument(), summary);
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
