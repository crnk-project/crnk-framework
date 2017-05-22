package io.crnk.gen.typescript.model;

public class TSPrimitiveType extends TSTypeBase {

	public static final TSPrimitiveType STRING = new TSPrimitiveType("string");

	public static final TSPrimitiveType NUMBER = new TSPrimitiveType("number");

	public static final TSPrimitiveType BOOLEAN = new TSPrimitiveType("boolean");

	public TSPrimitiveType(String name) {
		setName(name);
	}

	@Override
	public void accept(TSVisitor visitor) {
		visitor.visit(this);
	}
}
