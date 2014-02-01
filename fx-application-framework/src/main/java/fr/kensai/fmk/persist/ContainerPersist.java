package fr.kensai.fmk.persist;

import java.util.List;

import javafx.geometry.Orientation;

public class ContainerPersist {

	private final List<NodePersist> childs;
	private final Orientation orientation;
	private final double[] dividerPositions;

	public ContainerPersist(List<NodePersist> childs, Orientation orientation, double[] dividerPositions) {
		this.childs = childs;
		this.orientation = orientation;
		this.dividerPositions = dividerPositions;
	}

	public List<NodePersist> getChilds() {
		return childs;
	}

	public Orientation getOrientation() {
		return orientation;
	}

	public double[] getDividerPositions() {
		return dividerPositions;
	}

}
