package com.kensai.gui.views.orders;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.scene.control.TableCell;

import com.kensai.gui.services.model.orders.OrderModel;

public class OrderTimeTableCell extends TableCell<OrderModel, LocalDateTime> {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

	@Override
	public void updateItem(LocalDateTime item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setText(null);
			setGraphic(null);
			return;
		}

		setText(FORMATTER.format(item));
		setGraphic(null);
	}
}
