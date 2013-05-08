package fr.kensai.fmk.providing;

import java.util.List;

public interface DataListener {

	List<Class> getListenClasses();

	void selectionChanged(List<?> dataProviden);

}
