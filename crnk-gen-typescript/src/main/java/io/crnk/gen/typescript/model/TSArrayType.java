package io.crnk.gen.typescript.model;

public class TSArrayType extends TSTypeBase {

	private TSType elementType;

	public TSArrayType(TSType elementType) {
		this.elementType = elementType;
		setName("array<" + elementType.getName() + ">");
	}

	public TSType getElementType() {
		return elementType;
	}

	@Override
	public void accept(TSVisitor visitor) {
		visitor.visit(this);
	}
}
