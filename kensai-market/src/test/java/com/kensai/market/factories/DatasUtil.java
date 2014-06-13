package com.kensai.market.factories;

import static com.kensai.protocol.Trading.Role.ADMIN;
import static com.kensai.protocol.Trading.Role.FORBIDDEN;
import static com.kensai.protocol.Trading.Role.GROUPS;

import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.User;

public interface DatasUtil {

	String USER_DATA = "user_data";
	String USER_NAME = "user";
	String USER_GROUP = "default";
	User USER = User.newBuilder().setName(USER_NAME).addGroups(USER_GROUP).setOrderListeningRole(ADMIN).setExecListeningRole(ADMIN)
		.setIsListeningSummary(true).build();

	String UNKNOW_USER_NAME = DatasUtil.USER_NAME + " is unknow";
	User UNKNOW_USER = User.newBuilder().setName(UNKNOW_USER_NAME).setExecListeningRole(ADMIN).setOrderListeningRole(ADMIN)
		.setIsListeningSummary(true).build();

	String USER_UNLISTENER_NAME = DatasUtil.USER_NAME + " which listen nothing";
	User USER_UNLISTENER = User.newBuilder().setName(USER_UNLISTENER_NAME).setExecListeningRole(FORBIDDEN).setOrderListeningRole(FORBIDDEN)
		.setIsListeningSummary(false).build();

	String USER_LISTENER_NAME = DatasUtil.USER_NAME + " which listen USER";
	User USER_LISTENER = User.newBuilder().setName(USER_LISTENER_NAME).addGroups(USER_GROUP).setOrderListeningRole(GROUPS)
		.setExecListeningRole(GROUPS).addListeningGroupsOrder(USER_GROUP).addListeningGroupsExec(USER_GROUP).setIsListeningSummary(true).build();

	Instrument INSTRUMENT = Instrument.newBuilder().setIsin("isin").build();

}
