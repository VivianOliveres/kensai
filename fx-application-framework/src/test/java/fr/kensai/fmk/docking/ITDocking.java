package fr.kensai.fmk.docking;

import java.util.List;
import java.util.UUID;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import com.google.common.collect.Lists;

import fr.kensai.fmk.view.DefaultView;
import fr.kensai.fmk.view.View;
import fr.kensai.fmk.view.ViewFactory;

public class ITDocking extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		final TestViewFactory viewFactory = new TestViewFactory();
		List<ViewFactory> factories = Lists.newArrayList();
		factories.add(viewFactory);

		final RootDockingPane workspace = new RootDockingPane(factories);
		DockedContainer container = new DockedContainer(true, workspace);
		
		MenuBar bar = createMenuBar(workspace, factories);

		BorderPane root = new BorderPane();
		root.setTop(bar);
		root.setCenter(container);

		Scene scene = new Scene(root, 1000, 400);
		stage.setScene(scene);
		stage.show();

		workspace.createAndShowFloatingView(factories.get(0));
		workspace.createAndShowFloatingView(factories.get(0));
	}

	private MenuBar createMenuBar(final RootDockingPane workspace, final List<ViewFactory> factories) {

		MenuItem createViewItem = new MenuItem("New...");
		createViewItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				workspace.createAndShowFloatingView(factories.get(0));
			}
		});

		Menu menuView = new Menu("View");
		menuView.getItems().add(createViewItem);

		MenuBar bar = new MenuBar();
		bar.getMenus().add(menuView);
		
		return bar;
	}

	public static void main(String[] args) {
		launch(args);
	}
}

class TestViewFactory implements ViewFactory {

	@Override
	public String getGenericViewName() {
		return "TestView";
	}

	@Override
	public View createView(String viewName) {
		ListView<String> node = new ListView<>();
		for (int i = 0; i < 10; i++) {
			node.getItems().add(UUID.randomUUID().toString());
		}

		return new DefaultView(viewName, node);
	}

}
