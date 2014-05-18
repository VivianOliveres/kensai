package com.kensai.gui.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class MarketConnexionDescriptors implements Iterable<MarketConnectionDescriptor> {

	private List<MarketConnectionDescriptor> connexions = new ArrayList<>();

	public MarketConnexionDescriptors() {
		// No args constructor for deserialization
	}

	public MarketConnexionDescriptors(MarketConnectionDescriptor... connexions) {
		addAll(connexions);
	}

	public MarketConnexionDescriptors(List<MarketConnectionDescriptor> connexions) {
		addAll(connexions);
	}

	public void add(MarketConnectionDescriptor connexion) {
		connexions.add(connexion);
	}

	public void addAll(Collection<MarketConnectionDescriptor> connexions) {
		this.connexions.addAll(connexions);
	}

	public void addAll(MarketConnectionDescriptor... connexions) {
		for (MarketConnectionDescriptor marketConnexionDescriptor : connexions) {
			this.connexions.add(marketConnexionDescriptor);
		}
	}

	public List<MarketConnectionDescriptor> getConnexions() {
		return Lists.newArrayList(connexions);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), connexions);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof MarketConnexionDescriptors) {
			if (!super.equals(object))
				return false;
			MarketConnexionDescriptors that = (MarketConnexionDescriptors) object;
			return Objects.equal(this.connexions, that.connexions);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("super", super.toString()).add("connexions", connexions).toString();
	}

	@Override
	public Iterator<MarketConnectionDescriptor> iterator() {
		return connexions.iterator();
	}

	public int size() {
		return connexions.size();
	}

}
