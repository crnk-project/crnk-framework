package io.crnk.gen.typescript.transform;

import io.crnk.gen.typescript.model.TSContainerElement;

public class TSMetaTransformationOptions {

	public static final TSMetaTransformationOptions EMPTY = new TSMetaTransformationOptions();

	private TSContainerElement parent;

	public TSContainerElement getParent() {
		return parent;
	}

	/**
	 * @param parent where to place the generated object in the Typescript model.
	*  A new source file is typically generated if no parent is specified.
	 */
	public void setParent(TSContainerElement parent) {
		this.parent = parent;
	}
}