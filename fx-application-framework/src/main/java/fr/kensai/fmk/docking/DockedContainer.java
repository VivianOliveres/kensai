package fr.kensai.fmk.docking;

import java.util.List;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * SplitPane which contains only DockedContainer or TabPane (of DecoratedView) and manage dropped action.
 */
public class DockedContainer extends SplitPane {
	private static final Logger log = LogManager.getLogger(DockedContainer.class);

	private final RootDockingPane workspace;
	private final boolean isRootDockedContainer;

	public DockedContainer(boolean isRootDockedContainer, final RootDockingPane workspace) {
		this.isRootDockedContainer = isRootDockedContainer;
		this.workspace = workspace;

		// Listen items: if there is no more items in this container
		// then this container should be removed from its parent
		if (!isRootDockedContainer) {
			addEmptyItemsListener();
		}

		// Listening VIEW_DATA_FORMAT for DragAndDrop operations
		addDragOverListener();

		// Work for drag and drop operations
		addDragDroppedOperation();
	}

	private void addEmptyItemsListener() {
		getItems().addListener(new ListChangeListener<Node>() {

			@Override
			public void onChanged(Change<? extends Node> change) {
				DockedContainer thisContainer = DockedContainer.this;
				if (getItems().isEmpty()) {
					DockedContainer container = getParentDockedContainer(thisContainer.getParent());
					log.debug("Items becomes empty - remove this[{}] from parent [{}]", thisContainer, container);
					if (container == null) {
						log.error("onChanged - no container for {}", thisContainer);

					} else {
						container.getItems().remove(thisContainer);
					}
				}
			}
		});
	}

	private void addDragOverListener() {
		setOnDragOver(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				if (event.getDragboard().hasContent(RootDockingPane.VIEW_DATA_FORMAT)) {
					event.acceptTransferModes(TransferMode.MOVE);
				}

				event.consume();
			}
		});
	}

	private void addDragDroppedOperation() {
		setOnDragDropped(new EventHandler<DragEvent>() {
			@Override
			public void handle(final DragEvent event) {
				log.debug("onDragDropped: handle({})", event);

				// data dropped
				Dragboard db = event.getDragboard();
				if (db.hasContent(RootDockingPane.VIEW_DATA_FORMAT)) {
					// Retrieve point in scene coordinate system
					final Point2D point = new Point2D(event.getX(), event.getY());

					// Retrieve docking case before removing views
					final DockingCase dockingCase = getDockingCase(point);
					if (dockingCase == null) {
						// If here, there should be an error in algo methods
						log.error("No dockingCase was found - then do nothing");
						return;
					}

					// D&D view should be removed before being added
					// Else there is bugs with refresh (like SplitPane
					// showing empty panels)
					final DecoratedView view = workspace.getDecoratedView(event);
					removeView(view);

					// Make work after all updates from removeViewFromSource
					// to be sure that every binding operations from view removed
					// is finished (TODO is it really necessary??? to test)
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							try {
								// Make work
								doDragDropped(point, dockingCase, view);
								log.debug("Docking view is finished - workspace is: {}", workspace);

							} catch (RuntimeException e) {
								log.error("doDragDropped has failled", e);
							}
						}
					});
				}

				// let the source know whether the view was successfully
				// transferred and used
				event.setDropCompleted(true);
				event.consume();
			}
		});
	}

	/**
	 * According to point where view is dropped, then find which docking action should be done.
	 */
	private DockingCase getDockingCase(Point2D point) {
		Bounds boundsInScene = BoundsUtil.getBoundsInScene(this);
		log.debug("doDragDropped: {} - boundsInScene[{}]", point, boundsInScene);
		if (isCase0()) {
			return DockingCase.Case0;

		} else if (isCase1(point)) {
			return DockingCase.Case1;

		} else if (isCase2(point)) {
			return DockingCase.Case2;

		} else if (isCase3(point)) {
			return DockingCase.Case3;

		} else {
			log.error("Can not find any suitable case to dock view at [{}] in [{}]", point, BoundsUtil.toString(boundsInScene));
			return null;
		}
	}

	/**
	 * Make view docking according to given DockingCase
	 */
	private void doDragDropped(Point2D point, DockingCase dockingCase, DecoratedView view) {
		// Retrieve boolean case before removing views
		Bounds boundsInLocal = BoundsUtil.getBoundsInScene(this);
		log.debug("doDragDropped: {} - {} - boundsInLocal[{}], workspace is {}", point, dockingCase, boundsInLocal, workspace);

		if (dockingCase.equals(DockingCase.Case0)) {
			// Case 0: view is closed in empty root pane
			log.debug("doDragDropped - Case 0 - dock in main root pane");
			DockedTabPane pane = new DockedTabPane(workspace, false, view);
			getItems().add(pane);
			return;

		} else if (dockingCase.equals(DockingCase.Case1)) {
			// Case 1: view is closed to borders of this DockedContainer -> dock in parent container
			log.debug("doDragDropped - Case 1 - dock in parent - {} is in BoundsInLocal[{}] and not in dockable bounds[{}]", point,
				BoundsUtil.toString(boundsInLocal), BoundsUtil.toString(getDockableBounds()));
			dockInNearestParentSide(point, view);
			return;

		} else if (dockingCase.equals(DockingCase.Case2)) {
			// Case 2: view is closed of one of SplitPane divider -> add it to this DockedContainer
			log.debug("doDragDropped - Case 2 - dock in split pane divider - {} is in dividerBounds[{}]", point, toString(getChildrenDockableBounds()));
			DockedTabPane pane = new DockedTabPane(workspace, false, view);
			int index = getIndexToDockView(point);
			getItems().add(index, pane);
			return;

		} else if (dockingCase.equals(DockingCase.Case3)) {
			// Case 3: view is TabPane of one item of this -> dock as new Tab in TabPane
			log.debug("doDragDropped - Case 3 - dock in TabPane - {} is in ChildrenDockableBounds", point, toString(getChildrenDockableBounds()));
			int index = getIndexOfTabPane(point);
			((DockedTabPane) getItems().get(index)).getTabs().add(view);
			return;
		}

		log.error("doDragDropped - do nothing - {}", point);
	}

	private boolean isCase0() {
		boolean isCase0 = isRootDockedContainer && getItems().isEmpty();
		log.debug("isCase0 - return {}", isCase0);
		if (!isCase0 && isRootDockedContainer) {
			log.debug("isCase0 - isRootDockedContainer && items are {}", getItems());
		}
		return isCase0;
	}

	/**
	 * @return true if point is not in dockable bounds of this (i.e. point is not in 10 percent smaller bounds from this)
	 */
	private boolean isCase1(Point2D point) {
		Bounds dockableBounds = getDockableBounds();
		log.debug("isCase1: {} - point not in dockableBounds[{}]", !dockableBounds.contains(point), dockableBounds);
		return !dockableBounds.contains(point);
	}

	/**
	 * Dock given view in nearest parent pane.
	 */
	private void dockInNearestParentSide(Point2D point, DecoratedView view) {
		Bounds bounds = BoundsUtil.getBoundsInScene(this);
		if (BoundsUtil.isOnBottom(bounds, point)) {
			if (getOrientation().equals(Orientation.VERTICAL)) {
				log.debug("dockInNearestParentSide - isOnBottom && VERTICAL - {}", point);
				// Add view in a new TabPane at last index
				DockedTabPane pane = new DockedTabPane(workspace, false, view);
				getItems().add(pane);

			} else {
				log.debug("dockInNearestParentSide - isOnBottom && HORIZONTAL - {}", point);
				dockInNearestSide(Orientation.VERTICAL, false, view);
			}

		} else if (BoundsUtil.isOnLef(bounds, point)) {
			if (getOrientation().equals(Orientation.HORIZONTAL)) {
				log.debug("dockInNearestParentSide - isOnLef && HORIZONTAL - {}", point);
				// Add view in a new TabPane at first index
				DockedTabPane pane = new DockedTabPane(workspace, false, view);
				getItems().add(0, pane);

			} else {
				log.debug("dockInNearestParentSide - isOnLef && VERTICAL - {}", point);
				dockInNearestSide(Orientation.HORIZONTAL, true, view);
			}

		} else if (BoundsUtil.isOnRight(bounds, point)) {
			if (getOrientation().equals(Orientation.HORIZONTAL)) {
				log.debug("dockInNearestParentSide - isOnRight && HORIZONTAL - {}", point);
				// Add view in a new TabPane at last index
				DockedTabPane pane = new DockedTabPane(workspace, false, view);
				getItems().add(pane);

			} else {
				log.debug("dockInNearestParentSide - isOnRight && VERTICAL - {}", point);
				dockInNearestSide(Orientation.HORIZONTAL, false, view);
			}

		} else if (BoundsUtil.isOnTop(bounds, point)) {
			if (getOrientation().equals(Orientation.VERTICAL)) {
				log.debug("dockInNearestParentSide - isOnTop && VERTICAL - {}", point);
				// Add view in a new TabPane at first index
				DockedTabPane pane = new DockedTabPane(workspace, false, view);
				getItems().add(0, pane);

			} else {
				log.debug("dockInNearestParentSide - isOnTop && HORIZONTAL - {}", point);
				dockInNearestSide(Orientation.VERTICAL, true, view);
			}

		} else {
			log.error("dockInNearestParentSide - {} is close to no side -> BUG!!! - {}", point, BoundsUtil.toString(bounds));
		}
	}

	private void dockInNearestSide(Orientation orientation, boolean insertAtFirst, DecoratedView view) {
		// If there is only one item, then do NOT create another DockedContainer to insert in this.
		// Else there will be a explosion of DockedContainer with only one item in it.
		// TODO: This problem should be improved in another algorithm.
		if (getItems().size() == 1) {
			// Update orientation
			setOrientation(orientation);

			// Add to this, the new DockedContainer and a new TabPane with dragged view
			DockedTabPane pane = new DockedTabPane(workspace, false, view);
			if (insertAtFirst) {
				getItems().add(0, pane);

			} else {
				getItems().add(pane);
			}

		} else {
			// Create a new DockedContainer with all items than this
			DockedContainer thisCopy = new DockedContainer(false, workspace);
			thisCopy.getItems().addAll(getItems());

			// Update orientation
			setOrientation(orientation);

			// Add to this, the new DockedContainer and a new TabPane with dragged view
			DockedTabPane pane = new DockedTabPane(workspace, false, view);
			if (insertAtFirst) {
				getItems().setAll(pane, thisCopy);

			} else {
				getItems().setAll(thisCopy, pane);
			}
		}
	}

	/**
	 * Remove given view from its TabPane owner. If its TabPane owner becomes empty, then its items listener will clean
	 * it (see DockedTabPane)
	 */
	private void removeView(DecoratedView view) {
		log.debug("try to remove [{}] ", view);
		boolean removed = view.getTabPane().getTabs().remove(view);
		if (!removed) {
			log.error("View [{}] can not be removed from {}", view.getText(), view.getTabPane());
		}
	}

	/**
	 * @return first DockedContainer parent of this node. Return null if this node is null.
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

	/**
	 * @return true if point is in any Divider bounds from this split pane
	 */
	private boolean isCase2(Point2D point) {
		List<Bounds> bounds = getDividersBounds();
		for (Bounds b : bounds) {
			if (b.contains(point)) {
				log.debug("isCase2({}) - return true for DividerBounds [{}]", point, b);
				return true;
			}
		}

		// Not in any divider bounds then return false
		log.debug("isCase2({}) - return false for any DividerBounds [{}]", point, bounds);
		return false;
	}

	/**
	 * @return a list of bounds. Every bounds are centered on a divider of this split pane and have a width/height (if
	 *         orientation is horizontal/vertical) of 10% of neighbors panes.
	 */
	private List<Bounds> getDividersBounds() {
		List<Bounds> boundsToReturn = Lists.newArrayList();

		ObservableList<Node> items = getItems();
		for (int i = 1; i < items.size(); i++) {
			Bounds bounds;
			if (getOrientation().equals(Orientation.HORIZONTAL)) {
				Node nodeLeft = items.get(i - 1);
				Bounds leftBounds = BoundsUtil.getBoundsInScene(nodeLeft);
				double leftWidth = leftBounds.getWidth() / 10.0;
				double minX = leftBounds.getMaxX() - leftWidth;

				Node nodeRight = items.get(i);
				Bounds rightBounds = BoundsUtil.getBoundsInScene(nodeRight);
				double rightWidth = rightBounds.getWidth() / 10.0;

				bounds = new BoundingBox(minX, leftBounds.getMinY(), leftWidth + rightWidth, leftBounds.getHeight());
				log.debug("getDividersBounds - horizontal bounds [{}]", bounds);

			} else {
				// Vertical
				Node nodeTop = items.get(i - 1);
				Bounds topBounds = BoundsUtil.getBoundsInScene(nodeTop);
				double topHeight = topBounds.getHeight() / 10.0;

				Node nodeBottom = items.get(i);
				Bounds bottomBounds = BoundsUtil.getBoundsInScene(nodeBottom);
				double bottomHeight = bottomBounds.getHeight() / 10.0;
				double minY = bottomBounds.getMaxY() - bottomHeight;

				bounds = new BoundingBox(topBounds.getMinX(), minY, topBounds.getWidth(), topHeight + bottomHeight);
				log.debug("getDividersBounds - vertical bounds [{}]", bounds);
			}

			boundsToReturn.add(bounds);
		}

		log.debug("getDividersBounds - return [{}]", boundsToReturn);
		return boundsToReturn;
	}

	/**
	 * @param point
	 * @return true if point is in one dockable bounds item (i.e. in center of a TabPane).
	 */
	private boolean isCase3(Point2D point) {
		List<Bounds> childrenDockableBounds = getChildrenDockableBounds();
		for (Bounds bounds : childrenDockableBounds) {
			if (bounds.contains(point)) {
				return true;

			} else {
				log.debug("isCase3 - false for {}", bounds);
			}
		}

		// Not in any item dockable bounds (i.e. 10% smaller bounds) then return false
		log.debug("isCase3 - return false");
		return false;
	}

	/**
	 * @return index where to dock this view in SplitPane (i.e. where this point is the closest divider)
	 */
	private int getIndexToDockView(Point2D point) {
		int index = 0;
		double minDistance = Double.MAX_VALUE;
		for (int i = 0; i < getItems().size(); i++) {
			Node node = getItems().get(i);
			Bounds bounds = BoundsUtil.getBoundsInScene(node);
			if (getOrientation().equals(Orientation.HORIZONTAL)) {
				double distanceToLeft = Math.abs(point.getX() - bounds.getMinX());
				double distanceToRight = Math.abs(point.getX() - bounds.getMinX() - bounds.getWidth());
				if (distanceToLeft < minDistance) {
					index = i;
					minDistance = distanceToLeft;

				} else if (distanceToRight < minDistance) {
					index = i;
					minDistance = distanceToRight;
				}

			} else {
				// VERTICAL
				if (bounds.contains(point)) {
					double distanceToTop = Math.abs(point.getY() - bounds.getMinY());
					double distanceToBottom = Math.abs(point.getY() - bounds.getMinY() - bounds.getHeight());
					if (distanceToTop < minDistance) {
						index = i;
						minDistance = distanceToTop;

					} else if (distanceToBottom < minDistance) {
						index = i;
						minDistance = distanceToBottom;
					}
				}
			}
		}

		log.debug("getIndexToDockView - return index[{}] for {}", index, point);
		return index;
	}

	/**
	 * @return index of the pane which contains this point
	 */
	private int getIndexOfTabPane(Point2D point) {
		ObservableList<Node> nodes = getItems();
		for (int i = 0; i < nodes.size(); i++) {
			Node node = nodes.get(i);
			Bounds boundsInScene = BoundsUtil.getBoundsInScene(node);
			if (boundsInScene.contains(point)) {
				log.debug("getIndexOfTabPane - index [{}] from [{}] contains point [{}]", i, boundsInScene, point);
				return i;
			}
		}

		log.error("getIndexOfTabPane({}) - can not found valid index -> return -1");
		return -1;
	}

	public String toString(List<Bounds> bounds) {
		return Joiner.on(", ").join(bounds);
	}

	public Bounds getDockableBounds() {
		return BoundsUtil.getDockableBounds(this);
	}

	public List<Bounds> getChildrenDockableBounds() {
		List<Bounds> bounds = Lists.newArrayList();
		for (Node node : getItems()) {
			Bounds boundsInScene = BoundsUtil.getDockableBounds(node);
			log.debug("getChildrenDockableBounds - boundsInScene: {}", boundsInScene);
			bounds.add(boundsInScene);
		}

		return bounds;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "-" + getOrientation() + "[" + Joiner.on(", ").join(getItems()) + "]";
	}
}
