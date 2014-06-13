package com.kensai.market.matchers;

import org.mockito.ArgumentMatcher;

import com.kensai.protocol.Trading.CommandStatus;
import com.kensai.protocol.Trading.Messages;
import com.kensai.protocol.Trading.SubscribeCommand;

public class SubscribeCmdMatcher extends ArgumentMatcher {

	// Could be null
	private CommandStatus status;

	public SubscribeCmdMatcher() {
		this(null);
	}

	public SubscribeCmdMatcher(CommandStatus status) {
		this.status = status;
	}

	@Override
	public boolean matches(Object argument) {
		if (argument instanceof SubscribeCommand) {
			SubscribeCommand cmd = (SubscribeCommand) argument;
			return matchesStatus(cmd);

		} else if (argument instanceof Messages) {
			Messages msg = (Messages) argument;
			return msg.hasSubscribeCommand() && matchesStatus(msg.getSubscribeCommand());
		}

		return false;
	}

	public boolean matchesStatus(SubscribeCommand cmd) {
		if (status == null) {
			return true;

		} else {
			return status.equals(cmd.getStatus());
		}
	}

}
