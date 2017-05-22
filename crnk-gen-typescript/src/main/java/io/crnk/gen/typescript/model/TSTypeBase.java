package io.crnk.gen.typescript.model;

public abstract class TSTypeBase extends TSElementBase implements TSType {

	private String name;

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
