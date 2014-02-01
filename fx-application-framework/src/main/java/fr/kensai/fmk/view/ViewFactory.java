package fr.kensai.fmk.view;

public interface ViewFactory {

	String getGenericViewName();

	View createView(String viewName);

}
