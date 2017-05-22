package io.crnk.gen.typescript.model;

import java.util.List;

public interface TSContainerElement extends TSElement {

	public List<TSElement> getElements();

	public TSNamedElement getElement(String name);

	public void addElement(TSElement element);
}
