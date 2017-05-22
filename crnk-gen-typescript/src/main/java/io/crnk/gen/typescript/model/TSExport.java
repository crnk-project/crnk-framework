package io.crnk.gen.typescript.model;

public class TSExport extends TSImportExportBase {

	private boolean any;

	@Override
	public void accept(TSVisitor visitor) {
		visitor.visit(this);
	}

	public void setAny(boolean any) {
		this.any = any;
	}

	public boolean getAny() {
		return any;
	}
}
