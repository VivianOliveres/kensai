package com.kensai.animator.sdk;

import com.kensai.animator.core.MessageSender;
import com.kensai.protocol.Trading.Execution;
import com.kensai.protocol.Trading.ExecutionsSnapshot;
import com.kensai.protocol.Trading.InstrumentsSnapshot;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.OrdersSnapshot;
import com.kensai.protocol.Trading.SubscribeCommand;
import com.kensai.protocol.Trading.SummariesSnapshot;
import com.kensai.protocol.Trading.Summary;
import com.kensai.protocol.Trading.UnsubscribeCommand;

public interface Animator {

	void setMessageSender(MessageSender sender);

	void onSubscribe(SubscribeCommand cmd);

	void onUnsubscribe(UnsubscribeCommand cmd);

	void onSnapshot(SummariesSnapshot snapshot);

	void onSnapshot(OrdersSnapshot snapshot);

	void onSnapshot(ExecutionsSnapshot snapshot);

	void onSnapshot(InstrumentsSnapshot snapshot);

	void onOrder(Order order);

	void onExecution(Execution execution);

	void onSummary(Summary summary);

}
