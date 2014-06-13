package com.kensai.market.core;

import java.util.Set;

import org.jboss.netty.channel.Channel;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import com.kensai.protocol.Trading.Execution;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.Role;
import com.kensai.protocol.Trading.User;

public class UserCredentials {

	private final User user;
	private final Channel channel;

	public UserCredentials(User user, Channel channel) {
		this.user = user;
		this.channel = channel;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(user.getName());
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof UserCredentials) {
			UserCredentials that = (UserCredentials) object;
			return Objects.equal(this.user, that.user);
		}

		if (object instanceof User) {
			User that = (User) object;
			return Objects.equal(this.user, that);
		}

		return false;
	}

	public Channel getChannel() {
		return channel;
	}

	public User getUser() {
		return user;
	}

	public boolean isListeningOrderFrom(Order order) {
		return isListeningOrderFrom(order.getUser());
	}

	public boolean isListeningOrderFrom(User other) {
		Role role = user.getOrderListeningRole();
		if (role.equals(Role.ADMIN)) {
			return true;

		} else if (role.equals(Role.FORBIDDEN)) {
			return false;

		} else {
			// Role.GROUPS
			Set<String> otherGroups = Sets.newHashSet(other.getGroupsList());
			Set<String> userGroup = Sets.newHashSet(user.getListeningGroupsOrderList());
			return !Sets.intersection(userGroup, otherGroups).isEmpty();
		}
	}

	public boolean isListeningExecFrom(Execution exec) {
		return isListeningExecFrom(exec.getUser());
	}

	public boolean isListeningExecFrom(User other) {
		Role role = user.getExecListeningRole();
		if (role.equals(Role.ADMIN)) {
			return true;

		} else if (role.equals(Role.FORBIDDEN)) {
			return false;

		} else {
			// Role.GROUPS
			Set<String> otherGroups = Sets.newHashSet(other.getGroupsList());
			Set<String> userGroup = Sets.newHashSet(user.getListeningGroupsExecList());
			return !Sets.intersection(userGroup, otherGroups).isEmpty();
		}
	}

	public String getName() {
		return user.getName();
	}

	public boolean isListeningSummary() {
		return user.getIsListeningSummary();
	}
}
