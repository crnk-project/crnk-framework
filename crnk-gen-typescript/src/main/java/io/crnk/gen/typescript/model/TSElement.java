package io.crnk.gen.typescript.model;

public interface TSElement {

	TSElement getParent();

	void accept(TSVisitor visitor);

	void setParent(TSElement parent);

	TSType asType();
}
