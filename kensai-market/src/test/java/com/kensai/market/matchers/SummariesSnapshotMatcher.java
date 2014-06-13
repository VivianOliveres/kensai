package com.kensai.market.matchers;

import java.util.List;

import org.mockito.ArgumentMatcher;

import com.kensai.protocol.Trading.Messages;
import com.kensai.protocol.Trading.SummariesSnapshot;
import com.kensai.protocol.Trading.Summary;

public class SummariesSnapshotMatcher extends ArgumentMatcher {

	private List<Summary> summaries;

	public SummariesSnapshotMatcher() {
		this(null);
	}

	public SummariesSnapshotMatcher(List<Summary> summaries) {
		this.summaries = summaries;
	}

	@Override
	public boolean matches(Object argument) {
		if (argument instanceof SummariesSnapshot) {
			SummariesSnapshot cmd = (SummariesSnapshot) argument;
			return matchesSummaries(cmd);

		} else if (argument instanceof Messages) {
			Messages msg = (Messages) argument;
			return msg.hasSummariesSnapshot() && matchesSummaries(msg.getSummariesSnapshot());
		}

		return false;
	}

	private boolean matchesSummaries(SummariesSnapshot snapshot) {
		if (summaries == null) {
			return true;
		}

		return summaries.size() == snapshot.getSummariesCount();
	}

}
