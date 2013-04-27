package fr.kensai.fmk.docking;

import java.util.Map;

import javafx.collections.ListChangeListener;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import fr.kensai.fmk.providing.DataProvider;
import fr.kensai.fmk.providing.DataProviderService;
import fr.kensai.fmk.view.View;

/**
 * Simple Tab with a BorderPane layout where Top is a panel containing a Label which text is binded of tab textProperty.
 */
public class DecoratedView extends Tab {

	/**
	 * Label in HeaderPane to show view name
	 */
	private Label tabName = new Label();

	/**
	 * If view is ProviderListener, then create and show this combo box in HeaderPane.
	 */
	private ComboBox<DataProvider> providerComboBox;

	/**
	 * Content of this Tab. Top is the header (see {@link headerPane}). Center is the view content (see
	 * {@link View#getComponent()})
	 */
	private BorderPane mainPane = new BorderPane();

	/**
	 * Header of the view
	 */
	private AnchorPane headerPane = new AnchorPane();

	private final View view;

	public DecoratedView(View view) {
		this.view = view;
		setText(view.getViewName());
		setId(view.getViewName());

		// tabName label is binded to text Tab
		tabName.textProperty().bind(textProperty());
		tabName.getStyleClass().add("decorated-view-header-label");
		headerPane.getChildren().add(tabName);

		// tabName is anchored at left
		AnchorPane.setLeftAnchor(tabName, 5.0);
		AnchorPane.setTopAnchor(tabName, 2.5);
		AnchorPane.setBottomAnchor(tabName, 2.5);

		// Create providerComboBox
		Map<Class, ListChangeListener> dataProviderListeners = view.getDataProviderListeners();
		if (dataProviderListeners != null && !dataProviderListeners.isEmpty()) {
			providerComboBox = DataProviderService.getInstance().createDataProviderChooser(this);
			providerComboBox.getStyleClass().add("decorated-view-header-provider");
			headerPane.getChildren().add(providerComboBox);

			// providerComboBox is anchored at right
			AnchorPane.setRightAnchor(providerComboBox, 5.0);
			AnchorPane.setTopAnchor(providerComboBox, 2.5);
			AnchorPane.setBottomAnchor(providerComboBox, 2.5);
		}

		// Register view DataProvider (if any)
		if (view.getDataProviders() != null) {
			for (DataProvider provider : view.getDataProviders()) {
				DataProviderService.getInstance().addDataProvider(provider);
			}
		}

		// Init mainPane layout
		headerPane.getStyleClass().add("decorated-view-header");
		mainPane.getStyleClass().add("decorated-view");
		mainPane.setTop(headerPane);
		mainPane.setCenter(view.getComponent());

		// Init tab layout
		setContent(mainPane);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "View[" + getText() + "]";
	}

	public View getView() {
		return view;
	}

	/**
	 * @return ComboBox used to listen DataProviders from other views. Could be null if this view is listening no
	 *         DataProvider.
	 */
	public ComboBox<DataProvider> getProviderComboBox() {
		return providerComboBox;
	}
}
