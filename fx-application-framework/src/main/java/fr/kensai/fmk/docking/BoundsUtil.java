package fr.kensai.fmk.docking;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;

import com.google.common.primitives.Doubles;

public final class BoundsUtil {
	private BoundsUtil() {
		// Not instanciable
	}

	/**
	 * @return Bounds in scene coordinate system of the given node
	 */
	public static Bounds getBoundsInScene(Node node) {
		return node.localToScene(node.getBoundsInLocal());
	}

	/**
	 * @return 10 percent smaller bounds of the node in scene coordinate system
	 */
	public static Bounds getDockableBounds(Node node) {
		Bounds bounds = getBoundsInScene(node);
		return getDockableBounds(bounds);
	}

	/**
	 * @return a 10 percent smaller bounds
	 */
	public static Bounds getDockableBounds(Bounds bounds) {
		double minX = bounds.getMinX() + tenPercent(bounds.getWidth());
		double minY = bounds.getMinY() + tenPercent(bounds.getHeight());
		double width = bounds.getWidth() - 2 * tenPercent(bounds.getWidth());
		double height = bounds.getHeight() - 2 * tenPercent(bounds.getHeight());
		return new BoundingBox(minX, minY, width, height);
	}

	private static double tenPercent(double value) {
		if (value > 0) {
			return value / 10.0;

		} else {
			return 0;
		}
	}

	/**
	 * Utility method to make a simple and more explicit toString on a bounds
	 */
	public static String toString(Bounds bounds) {
		return "Bounds[x=" + bounds.getMinX() + ", y=" + bounds.getMinY() + ", width=" + bounds.getWidth() + ", height=" + bounds.getHeight() + "]";
	}

	/**
	 * @return true if this point is closest to top than any other side
	 */
	public static boolean isOnTop(Bounds bounds, Point2D point) {
		double distance = getTopDistance(bounds, point);
		return Doubles.min(distance, getBottomDistance(bounds, point), getRightDistance(bounds, point), getLeftDistance(bounds, point)) == distance;
	}

	/**
	 * @return true if this point is closest to bottom than any other side
	 */
	public static boolean isOnBottom(Bounds bounds, Point2D point) {
		double distance = getBottomDistance(bounds, point);
		return Doubles.min(distance, getTopDistance(bounds, point), getRightDistance(bounds, point), getLeftDistance(bounds, point)) == distance;
	}

	/**
	 * @return true if this point is closest to left than any other side
	 */
	public static boolean isOnLef(Bounds bounds, Point2D point) {
		double distance = getLeftDistance(bounds, point);
		return Doubles.min(distance, getTopDistance(bounds, point), getRightDistance(bounds, point), getBottomDistance(bounds, point)) == distance;
	}

	/**
	 * @return true if this point is closest to right than any other side
	 */
	public static boolean isOnRight(Bounds bounds, Point2D point) {
		double distance = getRightDistance(bounds, point);
		return Doubles.min(distance, getTopDistance(bounds, point), getLeftDistance(bounds, point), getBottomDistance(bounds, point)) == distance;
	}

	/**
	 * @return distance (>0) from this point and right border of given bounds
	 */
	public static double getRightDistance(Bounds bounds, Point2D point) {
		return Math.abs(point.getX() - bounds.getMinX() - bounds.getWidth());
	}

	/**
	 * @return distance (>0) from this point and left border of given bounds
	 */
	public static double getLeftDistance(Bounds bounds, Point2D point) {
		return Math.abs(point.getX() - bounds.getMinX());
	}

	/**
	 * @return distance (>0) from this point and top border of given bounds
	 */
	public static double getTopDistance(Bounds bounds, Point2D point) {
		return Math.abs(point.getY() - bounds.getMinY());
	}

	/**
	 * @return distance (>0) from this point and bottom border of given bounds
	 */
	public static double getBottomDistance(Bounds bounds, Point2D point) {
		return Math.abs(point.getY() - bounds.getMinY() - bounds.getHeight());
	}
}
