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
	public void addElement(TSElement element) {
		elements.add(element);
		element.setParent(this);
	}

	@Override
	public void addElement(int index, TSElement element) {
		elements.add(index, element);
		element.setParent(this);
	}
}
