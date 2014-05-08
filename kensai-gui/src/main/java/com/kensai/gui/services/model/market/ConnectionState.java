package com.kensai.gui.services.model.market;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;

import javafx.scene.image.Image;

public enum ConnectionState {

	DECONNECTED("market_icon_red_32x32.png"),
	CONNECTED("market_icon_green_32x32.png"), 
	CONNECTING("market_icon_blue_32x32.png");

	private final Image image;

	ConnectionState(String imageName) {
		URL resource = getClass().getClassLoader().getResource("icons/" + imageName);
		File file = new File(resource.getFile());
		try {
			image = new Image(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Can not find resource: " + resource, e);
		}
	}

	public Image getImage() {
		return image;
	}
}
