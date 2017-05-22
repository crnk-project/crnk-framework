package io.crnk.gen.typescript.model;

public interface TSElement {

	public TSElement getParent();

	public void accept(TSVisitor visitor);

	public void setParent(TSElement parent);

}
