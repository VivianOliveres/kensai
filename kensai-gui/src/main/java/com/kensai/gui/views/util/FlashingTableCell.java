package com.kensai.gui.views.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;

public class FlashingTableCell<S, T> extends TableCell<S, T> {

	public FlashingTableCell() {
		itemProperty().addListener(new ChangeListener<Object>() {

			@Override
			public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
				if (oldValue == null || !oldValue.equals(newValue)) {
					FlashAnimation<S, T> animation = new FlashAnimation<S, T>(FlashingTableCell.this);
					animation.playFromStart();
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean)
	 */
	@Override
	protected void updateItem(T item, boolean empty) {
		if (item == getItem()) {
			return;
		}

		super.updateItem(item, empty);

		if (item == null) {
			super.setText(null);
			super.setGraphic(null);

		} else {
			super.setText(item.toString());
			super.setGraphic(null);
		}
	}
}
