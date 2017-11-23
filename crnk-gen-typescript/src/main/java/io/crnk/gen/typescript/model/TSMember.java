package io.crnk.gen.typescript.model;

public abstract class TSMember extends TSElementBase {

	private String name;

	private TSType type;

	private boolean nullable;

	private boolean isPrivate;

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

	public abstract boolean isField();

	public abstract TSField asField();

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}
}
