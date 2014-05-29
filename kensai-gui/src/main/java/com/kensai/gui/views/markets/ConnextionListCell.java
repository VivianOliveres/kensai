package com.kensai.gui.views.markets;

import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;

import org.reactfx.EventStreams;
import org.reactfx.Subscription;

import com.kensai.gui.Images;
import com.kensai.gui.services.model.market.MarketConnectionModel;

public class ConnextionListCell extends ListCell<MarketConnectionModel> {

	private ImageView view;
	
	private MarketConnectionModel connexion;
	
	private Subscription nameSubscription;
	private Subscription streamSubscription;

	public ConnextionListCell() {
		this(new ImageView(Images.MARKET_RED_32));
	}

	public ConnextionListCell(ImageView view) {
		this.view = view;
	}

	@Override
	public void updateItem(MarketConnectionModel item, boolean isEmpty) {
		super.updateItem(item, isEmpty);
		if (item != null && item == connexion) {
			return;
		}

		if (connexion != null) {
			nameSubscription.unsubscribe();
			streamSubscription.unsubscribe();
		}

		if (item == null) {
			setText("");
			setGraphic(null);
			return;
		}

		setText(item.getConnectionName());
		setGraphic(view);

		nameSubscription = EventStreams.changesOf(item.connectionNameProperty())
												 .map(change -> change.getNewValue())
												 .subscribe(state -> setText(item.getConnectionName()));

		streamSubscription = EventStreams.changesOf(item.connectionStateProperty())
													.map(change -> change.getNewValue())
													.subscribe(state -> view.setImage(state.getImage()));
	}
}
