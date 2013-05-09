package fr.kensai.fmk.view;

import java.util.List;

import javafx.scene.Node;
import fr.kensai.fmk.providing.DataListener;
import fr.kensai.fmk.providing.DataProvider;

public interface View {

	void init();

	Class<? extends FactoryView> getFactoryClass();

	void setFactoryClass(Class<? extends FactoryView> factoryClass);

	String getViewName();

	DataListener getDataListener();

	List<DataProvider> getDataProviders();

	Node getComponent();

}
