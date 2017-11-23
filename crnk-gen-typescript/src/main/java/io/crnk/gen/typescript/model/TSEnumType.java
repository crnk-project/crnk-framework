package io.crnk.gen.typescript.model;

import java.util.ArrayList;
import java.util.List;

public class TSEnumType extends TSTypeBase implements TSExportedElement {

	private List<TSEnumLiteral> literals = new ArrayList<>();

	private boolean exported;

	@Override
	public boolean isExported() {
		return exported;
	}

	public void setExported(boolean exported) {
		this.exported = exported;
	}

	public List<TSEnumLiteral> getLiterals() {
		return literals;
	}

	@Override
	public void accept(TSVisitor visitor) {
		visitor.visit(this);
	}
}
