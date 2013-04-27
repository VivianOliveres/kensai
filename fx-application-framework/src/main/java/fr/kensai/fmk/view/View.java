package fr.kensai.fmk.view;

import java.util.List;
import java.util.Map;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import fr.kensai.fmk.providing.DataProvider;

public interface View {

	void init();

	void setFactoryClass(Class<? extends FactoryView> class1);

	String getViewName();

	Map<Class, ListChangeListener> getDataProviderListeners();

	List<DataProvider> getDataProviders();

	Node getComponent();

}
