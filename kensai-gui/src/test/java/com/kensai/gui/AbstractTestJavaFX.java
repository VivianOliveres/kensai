package com.kensai.gui;

import javafx.embed.swing.JFXPanel;

import org.junit.BeforeClass;

public class AbstractTestJavaFX {

	@BeforeClass
	public static void initJavaFX() {
		new JFXPanel();
	}

}
