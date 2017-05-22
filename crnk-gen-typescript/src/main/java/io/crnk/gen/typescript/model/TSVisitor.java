package io.crnk.gen.typescript.model;

public interface TSVisitor {

	public void visit(TSArrayType element);

	public void visit(TSEnumType element);

	public void visit(TSPrimitiveType element);

	public void visit(TSMember element);

	public void visit(TSIndexSignature element);

	public void visit(TSField element);

	public void visit(TSInterfaceType element);

	public void visit(TSAny tsAny);

	public void accept(TSSource file);

	public void accept(TSModule module);

	public void visit(TSImport importElement);

	public void visit(TSExport exportElement);

	public void visit(TSClassType classType);

	public void visit(TSParameterizedType parameterizedType);

}
