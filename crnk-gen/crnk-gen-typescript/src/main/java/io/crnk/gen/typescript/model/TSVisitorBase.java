package io.crnk.gen.typescript.model;

import java.util.ArrayList;

public class TSVisitorBase implements TSVisitor {

	@Override
	public void visit(TSArrayType element) {
		// nothing to do
	}

	@Override
	public void visit(TSEnumType element) {
		// nothing to do
	}

	@Override
	public void visit(TSPrimitiveType element) {
		// nothing to do
	}

	@Override
	public void visit(TSMember element) {
		// nothing to do
	}

	@Override
	public void visit(TSIndexSignatureType element) {
		// nothing to do
	}

	@Override
	public void visit(TSField element) {
		// nothing to do
	}

	@Override
	public void visit(TSInterfaceType element) {
		for (TSElement member : new ArrayList<>(element.getDeclaredMembers())) {
			member.accept(this);
		}
	}

	@Override
	public void visit(TSAny tsAny) {
		// nothing to do
	}

	@Override
	public void accept(TSSource source) {
		for (TSElement element : new ArrayList<>(source.getElements())) {
			element.accept(this);
		}
	}

	@Override
	public void accept(TSModule module) {
		for (TSElement element : new ArrayList<>(module.getElements())) {
			element.accept(this);
		}
	}

	@Override
	public void visit(TSImport importElement) {
		// nothing to do
	}

	@Override
	public void visit(TSExport tsExport) {
		// nothing to do
	}

	@Override
	public void visit(TSClassType classType) {
		for (TSElement element : new ArrayList<>(classType.getMembers())) {
			element.accept(this);
		}
	}

	@Override
	public void visit(TSParameterizedType parameterizedType) {
		// nothing to do
	}

	@Override
	public void visit(TSParameter parameter) {
		// nothing to do
	}

	@Override
	public void visit(TSFunction function) {
		// nothing to do
	}
}
