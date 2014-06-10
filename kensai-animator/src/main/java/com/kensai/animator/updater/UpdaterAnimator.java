package com.kensai.animator.updater;

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
import com.kensai.protocol.Trading.Execution;
import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.OrderAction;
import com.kensai.protocol.Trading.OrderStatus;
import com.kensai.protocol.Trading.SummariesSnapshot;
import com.kensai.protocol.Trading.Summary;

public class UpdaterAnimator extends AbstractSubscriberAnimator {

	private static final Logger log = LogManager.getLogger(UpdaterAnimator.class);

	private final Random random = new Random();

	private final String user;
	private final int minQty;
	private final int maxQty;

	private Map<Instrument, Summary> summaries = newHashMap();
	private Map<Instrument, Order> orders = newHashMap();

	private final Timer timer;

	public UpdaterAnimator(String user, int minQty, int maxQty, int delay, int period) {
		super(user);
		this.user = user;
		this.minQty = minQty;
		this.maxQty = maxQty;

		TimerTask task = createTask();
		timer = new Timer("Updater[" + user + "]", true);
		timer.schedule(task, delay, period);
	}

	private TimerTask createTask() {
		return new TimerTask() {

			@Override
			public void run() {
				log.info("doTimerTask");
				doUpdateTimerTask();
				log.info("doTimerTask - end");
			}
		};
	}

	private synchronized void doUpdateTimerTask() {
		log.info("doUpdateTimerTask");
		if (orders.isEmpty() || summaries.isEmpty()) {
			log.info("doUpdateTimerTask - orders.iSempty[{}] summaries.isEmpty[{}]", orders.isEmpty(), summaries.isEmpty());
			return;
		}

		log.info("Run task on {} orders", orders.size());
		// update each orders
		for (Order order : orders.values()) {
			if (order.getOrderStatus().equals(OrderStatus.TERMINATED)) {
				log.error("Invalid: order should not be Terminated into orders map: [{}]", order.getId());
			}

			if (order.getCommandStatus() == null) {
				log.warn("Do not update not acked or nacked order: [{}]", order.getId());
				continue;
			}

			// Update order
			Order.Builder builder = Order.newBuilder(order);
			builder.setAction(OrderAction.UPDATE);
			if (order.getSide().equals(BuySell.BUY)) {
				double price = round(order.getPrice() + 0.1);
				builder.setPrice(price);
			} else {
				double price = round(order.getPrice() - 0.1);
				builder.setPrice(price);
			}

			// Send order
			sendOrder(builder.build());
		}
	}

	private Order generateOrder(Instrument instrument) {
		Summary summary = summaries.get(instrument);
		int qty = generateQty(summary);
		double price = generatePrice(summary);
		BuySell side = random.nextBoolean() ? BuySell.BUY : BuySell.SELL;
		return generateOrder(instrument, qty, price, side);
	}

	private Order generateOrder(Instrument instrument, int qty, double price, BuySell side) {
		String userData = UserDataGenerator.generate();
		log.info("generate order [instr[{}] side[{}] qty[{}] price[{}] userData[{}] user[{}]]", new Object[] { instrument.getName(), side, qty, price, userData, user });
		return Order.newBuilder().setAction(OrderAction.INSERT)
										 .setInitialQuantity(qty)
										 .setInstrument(instrument)
										 .setPrice(price)
										 .setSide(side)
										 .setUserData(userData)
										 .setUser(user)
										 .build();
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

		double lowPrice = summary.getBuyDepths(summary.getBuyDepthsCount() - 1).getPrice() - 0.1;
		double highPrice = summary.getSellDepths(summary.getSellDepthsCount() - 1).getPrice() + 0.1;
		double factor = random.nextDouble();
		return round(lowPrice + factor * highPrice);
	}

	private void sendOrder(Order order) {
		getMessageSender().send(order);
	}

	@Override
	public synchronized void onSnapshot(SummariesSnapshot snapshot) {
		// Check preconditions
		if (snapshot == null || snapshot.getSummariesCount() <= 0) {
			log.warn("Receive an invalid summary snapshot: {}", snapshot);
			return;
		}

		// Initialize first order
		summaries.clear();
		for (Summary summary : snapshot.getSummariesList()) {
			summaries.put(summary.getInstrument(), summary);

			// Initialize with orders
			Order order = generateOrder(summary.getInstrument());
			orders.put(summary.getInstrument(), order);
			sendOrder(order);
		}
	}

	@Override
	public synchronized void onSummary(Summary summary) {
		Instrument instrument = summary.getInstrument();
		summaries.put(instrument, summary);
	}

	@Override
	public synchronized void onOrder(Order order) {
		// update orders map
		orders.put(order.getInstrument(), order);
	}

	@Override
	public synchronized void onExecution(Execution execution) {
		// re-send order if fully executed
		Order order = execution.getOrder();
		Instrument instrument = order.getInstrument();
		if (order.getOrderStatus().equals(OrderStatus.TERMINATED)) {
			Order newOrder = generateOrder(instrument);
			orders.put(instrument, newOrder);
			log.info("onExecution - order[{}] fully executed -> resend order", order.getId());
			sendOrder(newOrder);
		}
	}

	@Override
	public void setMessageSender(MessageSender sender) {
		super.setMessageSender(sender);

		// Send subscribe
		subscribe();
	}

}
