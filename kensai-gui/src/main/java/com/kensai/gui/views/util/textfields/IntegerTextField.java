package com.kensai.gui.views.util.textfields;

import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class IntegerTextField extends TextField {

	private final IntegerProperty numberProperty;

	public IntegerTextField(IntegerProperty numberProperty) {
		super(numberProperty.get() + "");
		this.numberProperty = numberProperty;

		textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
			try {
				numberProperty.set(Integer.valueOf(newValue));
			} catch (NumberFormatException ex) {
				/**
				 * Do nothing. newValue must not be able to be parsed as a double.
				 *
				 * There's a "better" way to check if newValue is parsable by Double.valueOf that is outlined at
				 * http://download.java.net/jdk8/docs/api/java/lang/Double.html#valueOf-java.lang.String- but it's simpler
				 * to catch the exception unless serious drawbacks to this approach are found.
				 */
			}
		});

		numberProperty.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			setText(String.valueOf(newValue));
		});
	}

	public IntegerProperty property() {
		return numberProperty;
	}
}