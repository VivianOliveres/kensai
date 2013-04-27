package fr.kensai.fmk.docking;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Joiner;

/**
 * TabPane of DecoratedView
 */
public class DockedTabPane extends TabPane {
	private static final Logger log = LogManager.getLogger(DockedTabPane.class);

	public DockedTabPane(final RootDockingPane workspace, boolean isInFloatingWindows, DecoratedView... tabs) {
		if (tabs != null && tabs.length > 0) {
			for (DecoratedView tab : tabs) {
				getTabs().add(tab);
			}
		}

		// Start DragAndDrop operation
		setOnDragDetected(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				DecoratedView view = (DecoratedView) getSelectionModel().getSelectedItem();
				if (view == null) {
					return;
				}

				log.debug("doOnDragDetected on [{}] - for {}", view.getText(), workspace);
				Dragboard db = startDragAndDrop(TransferMode.MOVE);

				ClipboardContent content = new ClipboardContent();
				content.put(RootDockingPane.VIEW_DATA_FORMAT, view.getText());
				db.setContent(content);

				event.consume();
			}
		});

		if (!isInFloatingWindows) {
			addListenerOnItems();
		}
	}

	/**
	 * When tabs list becomes empty, then parent should remove this empty TabPane
	 */
	private void addListenerOnItems() {
		getTabs().addListener(new ListChangeListener<Tab>() {

			@Override
			public void onChanged(Change<? extends Tab> change) {
				ObservableList<Tab> items = getTabs();
				if (items.isEmpty()) {
					DockedTabPane thisDockedTabPane = DockedTabPane.this;
					DockedContainer container = getParentDockedContainer(thisDockedTabPane.getParent());
					log.debug("Tabs becomes empty - remove [{}] from parent [{}]", thisDockedTabPane, container);
					if (container == null) {
						log.error("onChanged - no container for {}", thisDockedTabPane);

					} else {
						container.getItems().remove(thisDockedTabPane);
					}
				}
			}
		});
	}

	/**
	 * @return first parent which is an instance of DockedContainer of this node
	 */
	private DockedContainer getParentDockedContainer(Node node) {
		if (node == null) {
			return null;

		} else if (node instanceof DockedContainer) {
			return (DockedContainer) node;

		} else {
			return getParentDockedContainer(node.getParent());
		}
	}

	@Override
	public String toString() {
		return "DockedTabPane[" + Joiner.on(", ").join(getTabs()) + "]";
	}
}
