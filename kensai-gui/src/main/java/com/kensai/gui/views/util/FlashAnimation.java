package com.kensai.gui.views.util;

import javafx.animation.Transition;
import javafx.util.Duration;

public class FlashAnimation<S, T> extends Transition {

	private final FlashingTableCell<S, T> cell;

	public FlashAnimation(FlashingTableCell<S, T> cell) {
		this.cell = cell;

		setCycleDuration(Duration.millis(750));
		setAutoReverse(true);
		setCycleCount(1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javafx.animation.Transition#interpolate(double)
	 */
	@Override
	protected void interpolate(double frac) {
		if (cell == null || cell.getTableRow() == null || cell.getTableRow().getStyle() == null) {
			return;
		}

		if (frac >= 1) {
			cell.setStyle(cell.getTableRow().getStyle());

		} else {
			cell.setStyle("-fx-background-color: #FFFF00; -fx-text-fill: black;");
		}
	}

}
