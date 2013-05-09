package fr.kensai.fmk.view;

import java.util.List;

import javafx.scene.Node;
import fr.kensai.fmk.providing.DataListener;
import fr.kensai.fmk.providing.DataProvider;

public class DefaultView extends AbstractView {

	private final Node component;

	public DefaultView(String viewName, Node component) {
		super(viewName);
		this.component = component;
	}

	@Override
	public void init() {
		// Do nothing
	}

	@Override
	public DataListener getDataListener() {
		return null;
	}

	@Override
	public List<DataProvider> getDataProviders() {
		return null;
	}

	@Override
	public Node getComponent() {
		return component;
	}

}
