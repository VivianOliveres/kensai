package fr.kensai.fmk.providing;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DefaultDataProviderService implements DataProviderService {

	private List<DataProvider> providers = Lists.newArrayList();
	private Map<DataListener, ProviderController> listeners = Maps.newHashMap();

	@Override
	public void addProvider(DataProvider provider) {
		if (provider == null || providers.contains(provider)) {
			return;
		}

		providers.add(provider);
		for (DataListener listener : listeners.keySet()) {
			if (listener.getListenClasses().contains(provider.getProvidenClass())) {
				listeners.get(listener).addProvider(provider);
			}
		}
	}

	@Override
	public void removeProvider(DataProvider provider) {
		if (provider == null || !providers.contains(provider)) {
			return;
		}

		providers.remove(provider);

		for (DataListener listener : listeners.keySet()) {
			if (listener.getListenClasses().contains(provider.getProvidenClass())) {
				listeners.get(listener).removeProvider(provider);
			}
		}
	}

	@Override
	public ProviderController addListener(DataListener listener) {
		if (listeners.containsKey(listener)) {
			return listeners.get(listener);
		}

		List<DataProvider> providersToAssociate = newArrayList();
		for (Class<?> clazz : listener.getListenClasses()) {
			for (DataProvider provider : providers) {
				if (clazz.isAssignableFrom(provider.getProvidenClass())) {
					providersToAssociate.add(provider);
				}
			}
		}

		ProviderController controller = new ProviderController(listener, providersToAssociate);
		listeners.put(listener, controller);
		return controller;
	}

	@Override
	public void removeListener(DataListener listener) {
		listeners.remove(listener);
	}

}
