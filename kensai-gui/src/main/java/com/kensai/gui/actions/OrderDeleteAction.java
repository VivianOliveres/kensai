package com.kensai.gui.actions;

import javafx.scene.Node;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

import com.kensai.gui.services.connectors.MarketConnector;
import com.kensai.gui.services.model.instruments.InstrumentModel;
import com.kensai.gui.services.model.orders.OrderModel;

public class OrderDeleteAction {
	private static final Logger log = LogManager.getLogger(OrderDeleteAction.class);

	private final OrderModel order;
	private final Node guiDialogOwner;
	private final MarketConnector connector;

	public OrderDeleteAction(Node guiDialogOwner, OrderModel order, MarketConnector connector) {
		this.guiDialogOwner = guiDialogOwner;
		this.order = order;
		this.connector = connector;
	}

	public void execute() {
		InstrumentModel instrument = order.getInstrument();

		// Show confirm dialog
		Action response = Dialogs.create()
										 .owner(guiDialogOwner)
										 .title("Delete order")
										 .masthead("Delete order on " + instrument.getName())
										 .message("Do you want to delete order id[" + order.getId() + "]")
										 .showConfirm();

		if (response != Dialog.Actions.YES) {
			log.info("User click on [Cancel] or [No]");
			return;
		}

		// Send order delete
		log.info("sendDeleteOrder: {}", order);
		connector.sendDeleteOrder(order);
	}
}
