package fr.kensai.fmk.providing;

import fr.kensai.fmk.service.Service;

public interface DataProviderService extends Service {

	void addProvider(DataProvider provider);

	void removeProvider(DataProvider provider);

	ProviderController addListener(DataListener listener);

	void removeListener(DataListener listener);
}
