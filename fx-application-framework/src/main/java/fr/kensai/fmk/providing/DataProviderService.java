package fr.kensai.fmk.providing;

import javafx.scene.control.ComboBox;
import fr.kensai.fmk.docking.DecoratedView;

public class DataProviderService {

	private static final DataProviderService instance = new DataProviderService();

	public static DataProviderService getInstance() {
		return instance;
	}

	private DataProviderService() {
		// Not instanciable
	}

	public ComboBox<DataProvider> createDataProviderChooser(DecoratedView decoratedView) {
		throw new RuntimeException("Not yet implemented");
	}

	public void addDataProvider(DataProvider provider) {
		throw new RuntimeException("Not yet implemented");
	}

}
