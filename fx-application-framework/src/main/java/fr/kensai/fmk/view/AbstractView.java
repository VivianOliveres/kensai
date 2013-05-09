package fr.kensai.fmk.view;

public abstract class AbstractView implements View {

	private final String viewName;
	private Class<? extends FactoryView> factoryClass;

	public AbstractView(String viewName) {
		this.viewName = viewName;
	}

	@Override
	public String getViewName() {
		return viewName;
	}

	public void setFactoryClass(Class<? extends FactoryView> factoryClass) {
		this.factoryClass = factoryClass;
	}

	public Class<? extends FactoryView> getFactoryClass() {
		return factoryClass;
	}
}
