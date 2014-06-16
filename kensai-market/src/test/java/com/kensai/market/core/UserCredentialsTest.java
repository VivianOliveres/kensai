package com.kensai.market.core;

import static com.kensai.market.factories.DatasUtil.USER_GROUP;
import static com.kensai.market.factories.DatasUtil.USER_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.kensai.market.io.ChannelWritter;
import com.kensai.protocol.Trading.Role;
import com.kensai.protocol.Trading.User;

@RunWith(MockitoJUnitRunner.class)
public class UserCredentialsTest {

	private User.Builder builderAsAdmin = User.newBuilder().setName(USER_NAME).addGroups(USER_GROUP).setOrderListeningRole(Role.ADMIN)
		.setExecListeningRole(Role.ADMIN).setIsListeningSummary(true);

	private User.Builder builderAsOther = User.newBuilder().setName("toto").addGroups("toto").setOrderListeningRole(Role.FORBIDDEN)
		.setExecListeningRole(Role.FORBIDDEN).setIsListeningSummary(true);

	@Mock private ChannelWritter writer;

	private UserCredentials uc;

	@Test
	public void should_isListeningSummary_returns_true_when_user_isListeningSummary() {
		// GIVEN: UC wich listen summary
		User user = builderAsAdmin.build();
		uc = new UserCredentials(user, writer);

		// WHEN: isListeningSummary
		boolean isListening = uc.isListeningSummary();

		// THEN: True
		assertThat(isListening).isTrue();
	}

	@Test
	public void should_isListeningSummary_returns_false_when_user_not_isListeningSummary() {
		// GIVEN: UC wich listen summary
		User user = builderAsAdmin.setIsListeningSummary(false).build();
		uc = new UserCredentials(user, writer);

		// WHEN: isListeningSummary
		boolean isListening = uc.isListeningSummary();

		// THEN: False
		assertThat(isListening).isFalse();
	}

	@Test
	public void should_isListeningOrderFrom_returns_true_when_user_is_admin() {
		// GIVEN: UC with admin OrderRole
		User user = builderAsAdmin.build();
		uc = new UserCredentials(user, writer);

		// AND: Another user from another group
		User other = builderAsOther.build();

		// WHEN: isListeningOrderFrom
		boolean isListening = uc.isListeningOrderFrom(other);

		// THEN: True
		assertThat(isListening).isTrue();
	}

	@Test
	public void should_isListeningOrderFrom_returns_true_when_users_comes_from_same_group() {
		// GIVEN: UC with admin OrderGroupRole
		User user = builderAsAdmin.setOrderListeningRole(Role.GROUPS).addListeningGroupsOrder(USER_GROUP).build();
		uc = new UserCredentials(user, writer);

		// AND: Another user from same group
		User other = builderAsOther.addGroups(USER_GROUP).build();

		// WHEN: isListeningOrderFrom
		boolean isListening = uc.isListeningOrderFrom(other);

		// THEN: True
		assertThat(isListening).isTrue();
	}

	@Test
	public void should_isListeningOrderFrom_returns_false_when_users_listen_no_orders() {
		// GIVEN: UC with OrderRole Forbidden
		User user = builderAsAdmin.setOrderListeningRole(Role.FORBIDDEN).build();
		uc = new UserCredentials(user, writer);

		// AND: Another user from same group
		User other = builderAsOther.addGroups(USER_GROUP).setOrderListeningRole(Role.ADMIN).build();

		// WHEN: isListeningOrderFrom
		boolean isListening = uc.isListeningOrderFrom(other);

		// THEN: False
		assertThat(isListening).isFalse();
	}

	@Test
	public void should_isListeningOrderFrom_returns_false_when_users_from_another_group() {
		// GIVEN: UC with OrderRole Groups
		User user = builderAsAdmin.setOrderListeningRole(Role.FORBIDDEN).build();
		uc = new UserCredentials(user, writer);

		// AND: Another user from another group
		User other = builderAsOther.setOrderListeningRole(Role.ADMIN).build();

		// WHEN: isListeningOrderFrom
		boolean isListening = uc.isListeningOrderFrom(other);

		// THEN: False
		assertThat(isListening).isFalse();
	}

	@Test
	public void should_isListeningExecFrom_returns_true_when_user_is_admin() {
		// GIVEN: UC with admin ExecRole
		User user = builderAsAdmin.build();
		uc = new UserCredentials(user, writer);

		// AND: Another user from another group
		User other = builderAsOther.build();

		// WHEN: isListeningExecFrom
		boolean isListening = uc.isListeningExecFrom(other);

		// THEN: True
		assertThat(isListening).isTrue();
	}

	@Test
	public void should_isListeningExecFrom_returns_true_when_users_comes_from_same_group() {
		// GIVEN: UC with admin ExecGroup Role
		User user = builderAsAdmin.setExecListeningRole(Role.GROUPS).addListeningGroupsExec(USER_GROUP).build();
		uc = new UserCredentials(user, writer);

		// AND: Another user from same group
		User other = builderAsOther.addGroups(USER_GROUP).build();

		// WHEN: isListeningExecFrom
		boolean isListening = uc.isListeningExecFrom(other);

		// THEN: True
		assertThat(isListening).isTrue();
	}

	@Test
	public void should_isListeningExecFrom_returns_false_when_users_listen_no_orders() {
		// GIVEN: UC with ExecRole Forbidden
		User user = builderAsAdmin.setExecListeningRole(Role.FORBIDDEN).build();
		uc = new UserCredentials(user, writer);

		// AND: Another user from same group
		User other = builderAsOther.addGroups(USER_GROUP).setExecListeningRole(Role.ADMIN).build();

		// WHEN: isListeningExecFrom
		boolean isListening = uc.isListeningExecFrom(other);

		// THEN: False
		assertThat(isListening).isFalse();
	}

	@Test
	public void should_isListeningExecFrom_returns_false_when_users_listen_another_group() {
		// GIVEN: UC with Group Role
		User user = builderAsAdmin.setExecListeningRole(Role.GROUPS).build();
		uc = new UserCredentials(user, writer);

		// AND: Another user from same group
		User other = builderAsOther.setExecListeningRole(Role.ADMIN).build();

		// WHEN: isListeningExecFrom
		boolean isListening = uc.isListeningExecFrom(other);

		// THEN: False
		assertThat(isListening).isFalse();
	}

	@Test
	public void should_delegate_getName_to_user() {
		// GIVEN: UC
		User user = builderAsAdmin.setExecListeningRole(Role.GROUPS).build();
		uc = new UserCredentials(user, writer);

		// WHEN: getName
		String name = uc.getName();

		// THEN: names are identical
		assertThat(name).isEqualTo(user.getName());
	}

	@Test
	public void should_equals_worked_on_User() {
		// GIVEN: UC
		User user = builderAsAdmin.setExecListeningRole(Role.GROUPS).build();
		uc = new UserCredentials(user, writer);

		// WHEN: equals on User
		boolean equals = uc.equals(user);

		// THEN: True
		assertThat(equals).isTrue();
	}

	@Test
	public void should_equals_worked_on_UserCredentials() {
		// GIVEN: UC
		User user = builderAsAdmin.setExecListeningRole(Role.GROUPS).build();
		uc = new UserCredentials(user, writer);

		// WHEN: equals on User
		boolean equals = uc.equals(uc);

		// THEN: True
		assertThat(equals).isTrue();
	}
}
