package com.kensai.market.matchers;

import org.mockito.ArgumentMatcher;

import com.kensai.protocol.Trading.Execution;
import com.kensai.protocol.Trading.Messages;

public class ExecMatcher extends ArgumentMatcher {

	private Execution exec;

	public ExecMatcher() {
		this(null);
	}

	public ExecMatcher(Execution exec) {
		this.exec = exec;
	}

	@Override
	public boolean matches(Object argument) {
		if (argument instanceof Execution) {
			Execution cmd = (Execution) argument;
			return matchesExecution(cmd);

		} else if (argument instanceof Messages) {
			Messages msg = (Messages) argument;
			return msg.hasExecution() && matchesExecution(msg.getExecution());
		}

		return false;
	}

	private boolean matchesExecution(Execution otherExec) {
		if (exec == null) {
			return true;
		}

		return exec.equals(otherExec);
	}

}
