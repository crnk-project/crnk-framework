package io.crnk.gen.typescript.model;

import java.util.ArrayList;
import java.util.List;

public abstract class TSContainerElementBase extends TSElementBase implements TSContainerElement {

	private List<TSElement> elements = new ArrayList<>();

	@Override
	public List<TSElement> getElements() {
		return elements;
	}

	@Override
	public TSNamedElement getElement(String name) {
		for (TSElement element : elements) {
			if (element instanceof TSNamedElement) {
				TSNamedElement named = (TSNamedElement) element;
				if (name.equals(named.getName())) {
					return named;
				}
			}
		}
		return null;
	}

	@Override
	public void addElement(TSElement element) {
		elements.add(element);
		element.setParent(this);
	}
}
