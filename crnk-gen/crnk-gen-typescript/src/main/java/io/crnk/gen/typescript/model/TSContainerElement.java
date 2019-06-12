package io.crnk.gen.typescript.model;

import java.util.List;

public interface TSContainerElement extends TSElement {

	List<TSElement> getElements();

	void addElement(TSElement element);

	void addElement(int insertIndex, TSElement element);
}
