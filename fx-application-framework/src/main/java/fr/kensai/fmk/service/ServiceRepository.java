package fr.kensai.fmk.service;

import java.util.Map;

import com.google.common.collect.Maps;

public class ServiceRepository {

	private static ServiceRepository instance = new ServiceRepository();

	public static ServiceRepository getInstance() {
		return instance;
	}

	private Map<Class<? extends Service>, Service> services = Maps.newHashMap();

	private ServiceRepository() {
		// Not instanciable
	}

	public void register(Service service) {
		services.put(service.getClass(), service);
	}

	@SuppressWarnings("unchecked")
	public <T extends Service> T get(Class<? extends T> service) {
		return (T) services.get(service);
	}

}
