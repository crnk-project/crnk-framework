package io.crnk.gen.typescript.model;

public class TSInterfaceType extends TSObjectType {

	@Override
	public void accept(TSVisitor visitor) {
		visitor.visit(this);
	}

}
