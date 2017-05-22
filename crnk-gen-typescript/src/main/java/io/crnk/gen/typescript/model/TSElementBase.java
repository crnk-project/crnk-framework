package io.crnk.gen.typescript.model;

public abstract class TSElementBase implements TSElement {

	private TSElement parent;

	@Override
	public TSElement getParent() {
		return parent;
	}

	@Override
	public void setParent(TSElement parent) {
		this.parent = parent;
	}
}
