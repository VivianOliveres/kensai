package com.kensai.market.matchers;

import java.util.List;

import org.mockito.ArgumentMatcher;

import com.kensai.protocol.Trading.Execution;
import com.kensai.protocol.Trading.ExecutionsSnapshot;
import com.kensai.protocol.Trading.Messages;

public class ExecutionsSnapshotMatcher extends ArgumentMatcher {

	private List<Execution> execs;

	public ExecutionsSnapshotMatcher() {
		this(null);
	}

	public ExecutionsSnapshotMatcher(List<Execution> execs) {
		this.execs = execs;
	}

	@Override
	public boolean matches(Object argument) {
		if (argument instanceof ExecutionsSnapshot) {
			ExecutionsSnapshot cmd = (ExecutionsSnapshot) argument;
			return matchesOrders(cmd);

		} else if (argument instanceof Messages) {
			Messages msg = (Messages) argument;
			return msg.hasExecutionsSnapshot() && matchesOrders(msg.getExecutionsSnapshot());
		}

		return false;
	}

	private boolean matchesOrders(ExecutionsSnapshot snapshot) {
		if (execs == null) {
			return true;
		}

		return execs.size() == snapshot.getExecutionsCount();
	}

}
