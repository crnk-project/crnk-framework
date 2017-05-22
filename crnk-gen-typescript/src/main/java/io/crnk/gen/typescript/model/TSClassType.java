package io.crnk.gen.typescript.model;

public class TSClassType extends TSObjectType {

	private TSType superType;

	@Override
	public void accept(TSVisitor visitor) {
		visitor.visit(this);
	}

	public TSType getSuperType() {
		return superType;
	}

	public void setSuperType(TSType superType) {
		this.superType = superType;
	}

}
