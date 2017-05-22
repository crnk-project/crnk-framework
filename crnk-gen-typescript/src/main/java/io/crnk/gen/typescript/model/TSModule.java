package io.crnk.gen.typescript.model;

public class TSModule extends TSContainerElementBase implements TSNamedElement, TSExportedElement {

	private String name;

	private boolean exported;

	@Override
	public void accept(TSVisitor visitor) {
		visitor.accept(this);
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setExported(boolean exported) {
		this.exported = exported;
	}

	@Override
	public boolean isExported() {
		return exported;
	}
}
