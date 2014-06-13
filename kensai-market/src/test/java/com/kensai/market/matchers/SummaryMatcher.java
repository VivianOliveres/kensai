package com.kensai.market.matchers;

import org.mockito.ArgumentMatcher;

import com.kensai.protocol.Trading.Messages;
import com.kensai.protocol.Trading.Summary;

public class SummaryMatcher extends ArgumentMatcher {

	private Summary summary;

	public SummaryMatcher() {
		this(null);
	}

	public SummaryMatcher(Summary summary) {
		this.summary = summary;
	}

	@Override
	public boolean matches(Object argument) {
		if (argument instanceof Summary) {
			Summary order = (Summary) argument;
			return matchesOrder(order);

		} else if (argument instanceof Messages) {
			Messages msg = (Messages) argument;
			return msg.hasSummary() && matchesOrder(msg.getSummary());
		}

		return false;
	}

	private boolean matchesOrder(Summary otherSummary) {
		if (summary == null) {
			return true;
		}

		return summary.equals(otherSummary);
	}

}
