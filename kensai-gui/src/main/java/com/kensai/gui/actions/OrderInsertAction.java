package com.kensai.gui.actions;

import javafx.scene.Node;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;

import com.kensai.gui.services.connectors.MarketConnector;
import com.kensai.gui.services.model.orders.OrderModel;
import com.kensai.gui.views.summary.SendOrderViewController;

public class OrderInsertAction {
	private static final Logger log = LogManager.getLogger(OrderInsertAction.class);

	private final Node guiDialogOwner;
	private final OrderModel order;
	private final MarketConnector connector;

	public OrderInsertAction(Node guiDialogOwner, OrderModel order, MarketConnector connector) {
		this.guiDialogOwner = guiDialogOwner;
		this.order = order;
		this.connector = connector;
	}

	public void execute() {
		// Show dialog
		SendOrderViewController controller = new SendOrderViewController(order);
		Dialog dialog = new Dialog(guiDialogOwner, order.getSide() + " " + order.getInstrument().getName());
		dialog.setResizable(false);
		dialog.setIconifiable(false);
		dialog.setContent(controller.getView());
		dialog.getActions().addAll(controller.getAction(), Dialog.Actions.CANCEL);

		// Get user answer
		Action answer = dialog.show();
		if (answer.equals(Dialog.Actions.CANCEL)) {
			return;
		}

		// Send order
		log.info("sendOrder: {}", order);
		connector.sendInsertOrder(order);
	}
}
