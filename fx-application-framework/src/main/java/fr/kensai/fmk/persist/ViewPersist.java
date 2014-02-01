package fr.kensai.fmk.persist;

import fr.kensai.fmk.view.ViewFactory;

public class ViewPersist {

	private final Class<? extends ViewFactory> factoryClass;
	private final String viewName;

	public ViewPersist(Class<? extends ViewFactory> factoryClass, String viewName) {
		this.factoryClass = factoryClass;
		this.viewName = viewName;
	}

	public Class<? extends ViewFactory> getFactoryClass() {
		return factoryClass;
	}

	public String getViewName() {
		return viewName;
	}
}
