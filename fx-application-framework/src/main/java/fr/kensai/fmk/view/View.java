package fr.kensai.fmk.view;

import java.util.List;

import javafx.scene.Node;
import fr.kensai.fmk.providing.DataListener;
import fr.kensai.fmk.providing.DataProvider;

public interface View {

	void init();

	void setFactoryClass(Class<? extends FactoryView> class1);

	String getViewName();

	DataListener getDataListener();

	List<DataProvider> getDataProviders();

	Node getComponent();

}
