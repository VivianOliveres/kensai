package com.kensai.gui.views;

import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;

import org.reactfx.EventStreams;
import org.reactfx.Subscription;

import com.kensai.gui.Images;
import com.kensai.gui.services.model.market.MarketConnexionModel;

public class ConnextionListCell extends ListCell<MarketConnexionModel> {

	private ImageView view;
	
	private MarketConnexionModel connexion;
	private Subscription streamSubscription;

	public ConnextionListCell() {
		this(new ImageView(Images.MARKET_RED));
	}

	public ConnextionListCell(ImageView view) {
		this.view = view;
	}

	@Override
	public void updateItem(MarketConnexionModel item, boolean empty) {
		super.updateItem(item, empty);
		if (item == connexion) {
			return;
		}

		if (connexion != null) {
			streamSubscription.unsubscribe();
		}

		if (item == null) {
			setText("");
			setGraphic(null);
			return;
		}


		setText(item.getConnexionName());
		setGraphic(view);
		streamSubscription = EventStreams.changesOf(item.connectionStateProperty())
													.map(change -> change.getNewValue())
													.subscribe(state -> view.setImage(state.getImage()));
	}
}
