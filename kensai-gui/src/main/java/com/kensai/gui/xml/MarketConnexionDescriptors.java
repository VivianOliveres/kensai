package com.kensai.gui.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class MarketConnexionDescriptors implements Iterable<MarketConnexionDescriptor> {

	private List<MarketConnexionDescriptor> connexions = new ArrayList<>();

	public MarketConnexionDescriptors() {
		// No args constructor for deserialization
	}

	public MarketConnexionDescriptors(MarketConnexionDescriptor... connexions) {
		addAll(connexions);
	}

	public void add(MarketConnexionDescriptor connexion) {
		connexions.add(connexion);
	}

	public void addAll(Collection<MarketConnexionDescriptor> connexions) {
		this.connexions.addAll(connexions);
	}

	public void addAll(MarketConnexionDescriptor... connexions) {
		for (MarketConnexionDescriptor marketConnexionDescriptor : connexions) {
			this.connexions.add(marketConnexionDescriptor);
		}
	}

	public List<MarketConnexionDescriptor> getConnexions() {
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
	public Iterator<MarketConnexionDescriptor> iterator() {
		return connexions.iterator();
	}

}
