package io.crnk.gen.typescript.model;

public interface TSVisitor {

	void visit(TSArrayType element);

	void visit(TSEnumType element);

	void visit(TSPrimitiveType element);

	void visit(TSMember element);

	void visit(TSFunction function);

	void visit(TSParameter parameter);

	void visit(TSIndexSignature element);

	void visit(TSField element);

	void visit(TSInterfaceType element);

	void visit(TSAny tsAny);

	void accept(TSSource file);

	void accept(TSModule module);

	void visit(TSImport importElement);

	void visit(TSExport exportElement);

	void visit(TSClassType classType);

	void visit(TSParameterizedType parameterizedType);

}
