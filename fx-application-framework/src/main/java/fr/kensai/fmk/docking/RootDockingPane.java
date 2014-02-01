package fr.kensai.fmk.docking;

import java.util.List;
import java.util.Map;

import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Maps;

import fr.kensai.fmk.persist.ContainerPersist;
import fr.kensai.fmk.persist.NodePersist;
import fr.kensai.fmk.persist.ViewPersist;
import fr.kensai.fmk.view.ViewFactory;
import fr.kensai.fmk.view.View;

/**
 * RootPane containing referential for all views
 */
public class RootDockingPane extends BorderPane {
	private static final Logger log = LogManager.getLogger(RootDockingPane.class);

	public static final DataFormat VIEW_DATA_FORMAT = new DataFormat("VIEW_DATA_FORMAT");

	/**
	 * All views by their viewName. This is used to avoid to set view in DragContent (views can not be serialized)
	 */
	private Map<String, DecoratedView> views = Maps.newHashMap();

	/**
	 * Contains only DockedContainer or DecoratedView
	 */
	private DockedContainer root;

	/**
	 * All factories instances. Used when reloading layout to recreate View Instance(s).
	 */
	private List<ViewFactory> factories;

	public RootDockingPane(List<ViewFactory> factories) {
		this.factories = factories;
		root = new DockedContainer(true, this);
		setCenter(root);
	}

	public RootDockingPane(DecoratedView... viewsToAdd) {
		root = new DockedContainer(true, this);
		setCenter(root);

		// Default initialization of root
		if (viewsToAdd != null && viewsToAdd.length > 0) {
			for (DecoratedView view : viewsToAdd) {
				DockedTabPane pane = new DockedTabPane(this, false, view);
				root.getItems().add(pane);
				views.put(view.getText(), view);
			}
		}

		// Update dividers for SplitPane
		int size = root.getItems().size();
		double[] dividers = new double[size];
		for (int i = 0; i < size; i++) {
			dividers[i] = (i + 1) * (1.0 / (size + 0.0));
		}
		root.setDividerPositions(dividers);
	}

	public void initializeLayout(ContainerPersist rootPersisted) {
		if (rootPersisted == null) {
			log.info("Empty saved layout - do nothing");
			return;
		}

		// For each child, convert it to node and add it to root items
		for (NodePersist child : rootPersisted.getChilds()) {
			root.getItems().add(child.toNode(this));
		}

		// Update orientation and divider positions
		root.setOrientation(rootPersisted.getOrientation());
		root.setDividerPositions(rootPersisted.getDividerPositions());
	}

	public void createAndShowFloatingView(ViewFactory factory) {
		String viewName = generateUniqueViewName(factory.getGenericViewName());
		View view = factory.createView(viewName);
		view.init();
		view.setFactoryClass(factory.getClass());
		DecoratedView decoratedView = new DecoratedView(view);

		final DockedTabPane tabPane = new DockedTabPane(this, true, decoratedView);
		views.put(decoratedView.getText(), decoratedView);

		Scene sceneView = new Scene(tabPane);
		sceneView.getStylesheets().add("fx-app-fmk.css");

		final Stage stage = new Stage();
		stage.setScene(sceneView);
		stage.show();

		// Special listener to close Stage when Tab from TabPane is dropped elsewhere
		tabPane.getTabs().addListener(new ListChangeListener<Tab>() {

			@Override
			public void onChanged(ListChangeListener.Change<? extends Tab> change) {
				log.debug("tabPane becomes empty -> close");
				stage.close();
			}
		});
	}

	public DecoratedView createView(ViewPersist viewPersist) {
		// Retrieve FactoryView used to instanciate this view instance
		ViewFactory factory = null;
		for (ViewFactory viewFactory : factories) {
			if (viewFactory.getClass().equals(viewPersist.getFactoryClass())) {
				factory = viewFactory;
				break;
			}
		}

		// Error there is no factories for this view
		if (factory == null) {
			log.error("Can not create view [{}] - reason: FactoryView instance for [{}] does not exist in [{}]", viewPersist.getViewName(),
				viewPersist.getFactoryClass(), factories);
			return null;
		}

		// Build DecoratedView
		View view = factory.createView(viewPersist.getViewName());
		view.init();
		view.setFactoryClass(factory.getClass());
		DecoratedView decoratedView = new DecoratedView(view);
		views.put(decoratedView.getText(), decoratedView);
		return decoratedView;
	}

	/**
	 * @return a unique view named based on genericViewName all views with same name
	 */
	private String generateUniqueViewName(String genericViewName) {
		if (!views.containsKey(genericViewName)) {
			return genericViewName;
		}

		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			String viewName = genericViewName + "-" + i;
			if (views.containsKey(viewName)) {
				continue;
			}

			return viewName;
		}

		log.error("Can not generate unique view name for [{}]", genericViewName);
		return null;
	}

	public DecoratedView getDecoratedView(DragEvent event) {
		String viewName = event.getDragboard().getContent(VIEW_DATA_FORMAT).toString();
		return views.get(viewName);
	}

	@Override
	public String toString() {
		return "Workspace[" + root + "]";
	}

	public void addView(DecoratedView view) {
		String viewName = view.getView().getViewName();
		if (views.containsKey(viewName)) {
			log.warn("Put a view [{}] for an already existing DecoratedView: old[{}], new[{}]", viewName, views.get(viewName), view);
		}

		views.put(viewName, view);
	}

	public DockedContainer getRootPane() {
		return root;
	}

}
