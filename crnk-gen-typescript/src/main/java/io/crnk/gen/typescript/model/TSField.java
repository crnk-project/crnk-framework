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

	@Override
	public boolean isField() {
		return true;
	}

	@Override
	public TSField asField() {
		return this;
	}
}
