package com.kensai.animator.simple;

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

public class InitializerAnimator extends AbstractSubscriberAnimator {

	private static final Logger log = LogManager.getLogger(InitializerAnimator.class);

	private final User user;

	public InitializerAnimator(User user) {
		super(user);
		this.user = user;
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
	public void onSnapshot(SummariesSnapshot snapshot) {
		// Check preconditions
		if (snapshot == null || snapshot.getSummariesCount() <= 0) {
			log.warn("Receive an invalid summary snapshot: {}", snapshot);
			return;
		}

		Summary summary = snapshot.getSummariesList().get(0);
		Instrument instrument = summary.getInstrument();
		sendOrder(123, 456.789, BuySell.BUY, instrument);
	}

	@Override
	public void setMessageSender(MessageSender sender) {
		super.setMessageSender(sender);

		// Send subscribe
		subscribe();
	}

}
