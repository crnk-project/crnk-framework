package io.crnk.gen.typescript.writer;

import io.crnk.gen.typescript.model.*;

import java.util.HashSet;
import java.util.Set;

public class TSTypeReferenceResolver extends TSVisitorBase {

	private Set<TSType> types = new HashSet<>();

	public Set<TSType> getTypes() {
		return types;
	}

	@Override
	public void visit(TSArrayType element) {
		element.getElementType().accept(this);
	}

	@Override
	public void visit(TSInterfaceType element) {
		visitObjectType(element);
	}

	@Override
	public void visit(TSClassType element) {
		visitObjectType(element);
		if (element.getSuperType() != null) {
			addReference(element.getSuperType());
		}
	}

	private void visitObjectType(TSObjectType element) {
		if (element.getIndexSignature() != null) {
			element.getIndexSignature().accept(this);
		}
		for (TSElement member : element.getDeclaredMembers()) {
			member.accept(this);
		}
		for (TSInterfaceType interfaceType : element.getImplementedInterfaces()) {
			addReference(interfaceType);
		}
	}

	@Override
	public void visit(TSMember element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void visit(TSField element) {
		addReference(element.getType());
	}

	@Override
	public void visit(TSParameterizedType parameterizedType) {
		addReference(parameterizedType.getBaseType());
		for (TSType type : parameterizedType.getParameters()) {
			addReference(type);
		}
	}

	private void addReference(TSType type) {
		if (type.getParent() instanceof TSModule) {
			return;
		}

		if (type instanceof TSParameterizedType) {
			type.accept(this);
		} else if (type instanceof TSArrayType) {
			addReference(((TSArrayType) type).getElementType());
		} else {
			types.add(type);
		}
	}

	@Override
	public void accept(TSSource file) {
		for (TSElement element : file.getElements()) {
			element.accept(this);
		}
	}

	@Override
	public void accept(TSModule module) {
		for (TSElement element : module.getElements()) {
			element.accept(this);
		}
	}

	@Override
	public void visit(TSIndexSignature element) {
		addReference(element.getKeyType());
		addReference(element.getValueType());
	}
}
