package fr.kensai.fmk.view;

public abstract class AbstractView implements View {

	private final String viewName;
	private Class<? extends ViewFactory> factoryClass;

	public AbstractView(String viewName) {
		this.viewName = viewName;
	}

	@Override
	public String getViewName() {
		return viewName;
	}

	public void setFactoryClass(Class<? extends ViewFactory> factoryClass) {
		this.factoryClass = factoryClass;
	}

	public Class<? extends ViewFactory> getFactoryClass() {
		return factoryClass;
	}
}
