package fr.kensai.fmk.providing;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

public class ProviderController {

	private final DataListener listener;

	private final ObservableList<DataProvider> providers = FXCollections.observableArrayList();
	private final ComboBox<DataProvider> combo = new ComboBox<>();

	private final EmptyDataProvider emptyProvider = new EmptyDataProvider();

	public ProviderController(DataListener listener) {
		this(listener, new ArrayList<DataProvider>());
	}

	public ProviderController(DataListener listener, List<DataProvider> providers) {
		this.listener = listener;
		this.providers.addAll(providers);

		initCombo();
	}

	private void initCombo() {
		combo.valueProperty().addListener(new ChangeListener<DataProvider>() {
			@Override
			public void changed(ObservableValue<? extends DataProvider> observable, DataProvider oldValue, DataProvider newValue) {
				if (oldValue != null) {
					oldValue.removeListener(listener);
				}

				if (newValue != null) {
					newValue.addListener(listener);
				}
			}
		});

		providers.add(0, emptyProvider);
		combo.setItems(providers);
	}

	public EmptyDataProvider getDefaultProvider() {
		return emptyProvider;
	}

	public DataListener getListener() {
		return listener;
	}

	public ComboBox<DataProvider> getComboBox() {
		return combo;
	}

	public void addProvider(DataProvider provider) {
		if (!providers.contains(provider)) {
			providers.add(provider);
		}
	}

	public void removeProvider(DataProvider provider) {
		providers.remove(provider);
	}
}
