package io.crnk.gen.typescript.model;

public class TSParameter extends TSElementBase {

	private String name;

	private TSType type;

	private boolean nullable;

	public TSParameter(String name, TSType type, boolean nullable) {
		this.name = name;
		this.type = type;
		this.nullable = nullable;
	}


	public String getName() {
		return name;
	}

	public TSType getType() {
		return type;
	}

	public boolean isNullable() {
		return nullable;
	}

	public TSType getElementType() {
		if (type instanceof TSArrayType) {
			return ((TSArrayType) type).getElementType();
		}
		return type;
	}

	@Override
	public void accept(TSVisitor visitor) {
		visitor.visit(this);
	}

}