package com.kensai.gui.actions;

import javafx.scene.Node;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;

import com.kensai.gui.services.connectors.MarketConnector;
import com.kensai.gui.services.model.instruments.InstrumentModel;
import com.kensai.gui.services.model.orders.OrderModel;
import com.kensai.gui.views.instruments.SendOrderViewController;

public class OrderUpdateAction {
	private static final Logger log = LogManager.getLogger(OrderUpdateAction.class);

	private final Node guiDialogOwner;
	private final OrderModel order;
	private final MarketConnector connector;

	public OrderUpdateAction(Node guiDialogOwner, OrderModel order, MarketConnector connector) {
		this.guiDialogOwner = guiDialogOwner;
		this.order = order;
		this.connector = connector;
	}

	public void execute() {
		InstrumentModel instrument = order.getInstrument();

		// Show dialog
		SendOrderViewController controller = new SendOrderViewController(order);
		Dialog dialog = new Dialog(guiDialogOwner, "Update order on " + instrument.getName());
		dialog.setResizable(false);
		dialog.setIconifiable(false);
		dialog.setContent(controller.getView());
		dialog.getActions().addAll(controller.getAction(), Dialog.Actions.CANCEL);

		// Get user answer
		Action answer = dialog.show();
		if (answer.equals(Dialog.Actions.CANCEL)) {
			return;
		}

		// Send update order
		log.info("sendUpdateOrder: {}", order);
		connector.sendUpdateOrder(order);
	}
}
