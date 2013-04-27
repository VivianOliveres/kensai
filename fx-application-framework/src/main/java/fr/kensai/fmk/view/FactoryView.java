package fr.kensai.fmk.view;

public interface FactoryView {

	String getGenericViewName();

	View createView(String viewName);

}
