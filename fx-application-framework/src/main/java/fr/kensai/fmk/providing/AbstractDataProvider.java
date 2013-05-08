package fr.kensai.fmk.providing;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractDataProvider implements DataProvider {

	private static final Logger log = LogManager.getLogger(AbstractDataProvider.class);

	private final List<DataListener> listeners = newArrayList();

	private final List providenDatas = newArrayList();

	@Override
	public void removeListener(DataListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void addListener(DataListener listener) {
		listeners.add(listener);
		listener.selectionChanged(newArrayList());
	}

	@Override
	public void notifyListeners() {
		for (DataListener listener : listeners) {
			try {
				listener.selectionChanged(newArrayList());

			} catch (RuntimeException e) {
				log.error("Can not provide selection change", e);
			}
		}
	}

	@Override
	public List<?> getProvidenDatas() {
		return providenDatas;
	}

	public void setProvidenDatas(List<?> datas) {
		providenDatas.clear();
		providenDatas.addAll(datas);
	}

}
