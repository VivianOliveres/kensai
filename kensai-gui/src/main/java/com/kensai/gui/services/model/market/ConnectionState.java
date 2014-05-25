package com.kensai.gui.services.model.market;

import javafx.scene.image.Image;

import com.kensai.gui.Images;

public enum ConnectionState {


	DISCONNECTED(Images.MARKET_RED_32), CONNECTED(Images.MARKET_GREEN_32), CONNECTING(Images.MARKET_ORANGE_32);

	private final Image image;

	ConnectionState(Image image) {
		this.image = image;
	}

	public Image getImage() {
		return image;
	}
}
