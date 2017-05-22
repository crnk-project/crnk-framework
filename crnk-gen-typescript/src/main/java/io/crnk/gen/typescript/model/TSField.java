package io.crnk.gen.typescript.model;

public class TSField extends TSMember {

	private String initializer;

	@Override
	public void accept(TSVisitor visitor) {
		visitor.visit(this);
	}

	public String getInitializer() {
		return initializer;
	}

	public void setInitializer(String initializer) {
		this.initializer = initializer;
	}
}
