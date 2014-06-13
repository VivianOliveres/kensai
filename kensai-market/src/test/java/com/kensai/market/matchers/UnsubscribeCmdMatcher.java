package com.kensai.market.matchers;

import org.mockito.ArgumentMatcher;

import com.kensai.protocol.Trading.CommandStatus;
import com.kensai.protocol.Trading.Messages;
import com.kensai.protocol.Trading.UnsubscribeCommand;

public class UnsubscribeCmdMatcher extends ArgumentMatcher {

	// Could be null
	private CommandStatus status;

	public UnsubscribeCmdMatcher() {
		this(null);
	}

	public UnsubscribeCmdMatcher(CommandStatus status) {
		this.status = status;
	}

	@Override
	public boolean matches(Object argument) {
		if (argument instanceof UnsubscribeCommand) {
			UnsubscribeCommand cmd = (UnsubscribeCommand) argument;
			return matchesStatus(cmd);

		} else if (argument instanceof Messages) {
			Messages msg = (Messages) argument;
			return msg.hasUnsubscribeCommand() && matchesStatus(msg.getUnsubscribeCommand());
		}

		return false;
	}

	public boolean matchesStatus(UnsubscribeCommand cmd) {
		if (status == null) {
			return true;

		} else {
			return status.equals(cmd.getStatus());
		}
	}

}
