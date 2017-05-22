package io.crnk.gen.typescript.model;

public class TSAny extends TSTypeBase {

	public static final TSAny INSTANCE = new TSAny();

	private TSAny() {
		setName("any");
	}

	@Override
	public void accept(TSVisitor visitor) {
		visitor.visit(this);
	}
}
