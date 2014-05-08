package com.kensai.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;

import javafx.scene.image.Image;

public interface Images {

	Image MARKET_RED = create("market_icon_red_32x32.png");
	Image MARKET_GREEN = create("market_icon_green_32x32.png");
	Image MARKET_BLUE = create("market_icon_blue_32x32.png");

	Image ADD = create("Add_16x16.png");
	Image EDIT = create("Modify_16x16.png");
	Image REMOVE = create("Remove_16x16.png");

	public static Image create(String imageName) {
		URL resource = Images.class.getClassLoader().getResource("icons/" + imageName);
		File file = new File(resource.getFile());
		try {
			return new Image(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Can not find resource: " + resource, e);
		}
	}
}
