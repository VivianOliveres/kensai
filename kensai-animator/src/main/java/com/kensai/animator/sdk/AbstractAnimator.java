package com.kensai.animator.sdk;

import com.kensai.protocol.Trading.Execution;
import com.kensai.protocol.Trading.ExecutionsSnapshot;
import com.kensai.protocol.Trading.InstrumentsSnapshot;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.OrdersSnapshot;
import com.kensai.protocol.Trading.SubscribeCommand;
import com.kensai.protocol.Trading.SummariesSnapshot;
import com.kensai.protocol.Trading.Summary;
import com.kensai.protocol.Trading.UnsubscribeCommand;

public abstract class AbstractAnimator implements Animator {

	@Override
	public void onSubscribe(SubscribeCommand cmd) {
		// No implementation by default
	}

	@Override
	public void onUnsubscribe(UnsubscribeCommand cmd) {
		// No implementation by default
	}

	@Override
	public void onSnapshot(SummariesSnapshot snapshot) {
		// No implementation by default
	}

	@Override
	public void onSnapshot(OrdersSnapshot snapshot) {
		// No implementation by default
	}

	@Override
	public void onSnapshot(ExecutionsSnapshot snapshot) {
		// No implementation by default
	}

	@Override
	public void onSnapshot(InstrumentsSnapshot snapshot) {
		// No implementation by default
	}

	@Override
	public void onOrder(Order order) {
		// No implementation by default
	}

	@Override
	public void onExecution(Execution execution) {
		// No implementation by default
	}

	@Override
	public void onSummary(Summary summary) {
		// No implementation by default
	}

	protected double round(double price) {
		double floor = Math.floor(price);
		double rest = price - floor;
		if (rest < 0.5) {
			return floor;

		} else {
			return floor + 0.5;
		}
	}
}
