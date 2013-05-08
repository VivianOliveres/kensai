package fr.kensai.fmk.providing;

import java.util.List;

public interface DataProvider {

	String getName();

	void removeListener(DataListener listener);

	void addListener(DataListener listener);

	void notifyListeners();

	Class getProvidenClass();

	List<?> getProvidenDatas();
}
