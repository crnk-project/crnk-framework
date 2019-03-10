package io.crnk.gen.typescript.model;

public class TSImport extends TSImportExportBase {

	@Override
	public void accept(TSVisitor visitor) {
		visitor.visit(this);
	}
}