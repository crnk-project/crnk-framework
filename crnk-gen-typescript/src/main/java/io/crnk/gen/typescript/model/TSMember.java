package io.crnk.gen.typescript.model;

public abstract class TSMember extends TSElementBase {

	private String name;

	private TSType type;

	private boolean nullable;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public TSType getType() {
		return type;
	}

	public void setType(TSType type) {
		this.type = type;
	}

	public boolean isNullable() {
		return nullable;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	public TSType getElementType() {
		if (type instanceof TSArrayType) {
			return ((TSArrayType) type).getElementType();
		}
		return type;
	}

}
