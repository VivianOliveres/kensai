package com.kensai.gui.views;

import static org.assertj.core.api.Assertions.assertThat;
import javafx.scene.image.ImageView;

import org.junit.Before;
import org.junit.Test;

import com.kensai.gui.AbstractTestJavaFX;
import com.kensai.gui.Images;
import com.kensai.gui.services.model.market.ConnectionState;
import com.kensai.gui.services.model.market.MarketConnectionModel;
import com.kensai.gui.views.markets.ConnextionListCell;

public class ConnextionListCellTest extends AbstractTestJavaFX {

	private MarketConnectionModel CAC_CONNEXION = new MarketConnectionModel("CAC", "localhost", 1664, true);

	private ImageView view;
	private ConnextionListCell cell;

	@Before
	public void init() {
		view = new ImageView(Images.MARKET_RED_32);
		cell = new ConnextionListCell(view);
	}

	@Test
	public void should_update_cell() {
		// WHEN: update cell item
		cell.updateItem(CAC_CONNEXION, false);

		// THEN: cell is updated
		assertThat(cell.getText()).isEqualTo(CAC_CONNEXION.getConnectionName());
		assertThat(view.getImage()).isEqualTo(CAC_CONNEXION.getConnectionState().getImage());
	}

	@Test
	public void should_update_cell_image_when_connexionState_is_updated() {
		// GIVEN: cell is updated to CAC item
		MarketConnectionModel swxConnexion = new MarketConnectionModel("SWX", "localhost", 4661, false);
		cell.updateItem(swxConnexion, false);

		// WHEN: update connexionState
		swxConnexion.setConnectionState(ConnectionState.CONNECTED);

		// THEN: cell image is updated
		assertThat(view.getImage()).isEqualTo(ConnectionState.CONNECTED.getImage());
	}
}
