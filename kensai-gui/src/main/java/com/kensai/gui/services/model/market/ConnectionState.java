package com.kensai.gui.services.model.market;

import javafx.scene.image.Image;

import com.kensai.gui.Images;

public enum ConnectionState {


	DISCONNECTED(Images.MARKET_RED), CONNECTED(Images.MARKET_GREEN), CONNECTING(Images.MARKET_BLUE);

	private final Image image;

	ConnectionState(Image image) {
		this.image = image;
	}

	public Image getImage() {
		return image;
	}
}
