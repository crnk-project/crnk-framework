package io.crnk.gen.typescript.writer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import io.crnk.gen.typescript.model.TSAny;
import io.crnk.gen.typescript.model.TSArrayType;
import io.crnk.gen.typescript.model.TSClassType;
import io.crnk.gen.typescript.model.TSElement;
import io.crnk.gen.typescript.model.TSEnumLiteral;
import io.crnk.gen.typescript.model.TSEnumType;
import io.crnk.gen.typescript.model.TSExport;
import io.crnk.gen.typescript.model.TSExportedElement;
import io.crnk.gen.typescript.model.TSField;
import io.crnk.gen.typescript.model.TSImport;
import io.crnk.gen.typescript.model.TSIndexSignature;
import io.crnk.gen.typescript.model.TSInterfaceType;
import io.crnk.gen.typescript.model.TSMember;
import io.crnk.gen.typescript.model.TSModule;
import io.crnk.gen.typescript.model.TSParameterizedType;
import io.crnk.gen.typescript.model.TSPrimitiveType;
import io.crnk.gen.typescript.model.TSSource;
import io.crnk.gen.typescript.model.TSType;
import io.crnk.gen.typescript.model.TSVisitor;

public class TSWriter implements TSVisitor {

	private int currentIndentation = 0;

	private StringBuilder builder = new StringBuilder();

	private TSCodeStyle codeStyle;

	public TSWriter(TSCodeStyle codeStyle) {
		this.codeStyle = codeStyle;
	}

	@Override
	public void visit(TSInterfaceType element) {
		appendLine();

		appendExported(element);

		builder.append("interface ");
		builder.append(element.getName());
		if (!element.getImplementedInterfaces().isEmpty()) {
			builder.append(" extends ");
			sortAndAppendTypes(element.getImplementedInterfaces());
		}

		startScope();

		if (element.getIndexSignature() != null) {
			element.getIndexSignature().accept(this);
		}

		for (TSMember member : element.getMembers()) {
			member.accept(this);
		}
		endScope();
	}

	private void sortAndAppendTypes(Set<TSInterfaceType> types) {
		List<TSInterfaceType> sorted = new ArrayList<>(types);
		Collections.sort(sorted, new Comparator<TSInterfaceType>() {

			@Override
			public int compare(TSInterfaceType type0, TSInterfaceType type1) {
				return type0.getName().compareTo
						(type1.getName());
			}
		});


		Iterator<TSInterfaceType> iterator = sorted.iterator();
		while (iterator.hasNext()) {
			TSInterfaceType implementedInterface = iterator.next();
			visitReference(implementedInterface);
			if (iterator.hasNext()) {
				builder.append(", ");
			}
		}
	}

	@Override
	public void visit(TSClassType element) {
		appendLine();

		appendExported(element);

		builder.append("class ");
		builder.append(element.getName());
		if (element.getSuperType() != null) {
			builder.append(" extends ");
			visitReference(element.getSuperType());
		}
		if (!element.getImplementedInterfaces().isEmpty()) {
			builder.append(" implements ");
			sortAndAppendTypes(element.getImplementedInterfaces());
		}

		startScope();

		if (element.getIndexSignature() != null) {
			element.getIndexSignature().accept(this);
		}

		for (TSMember member : element.getMembers()) {
			member.accept(this);
		}
		endScope();
	}

	private void appendExported(TSExportedElement element) {
		if (element.isExported()) {
			builder.append("export ");
		}
	}

	public void visitReference(TSModule module) {
		if (module.getParent() instanceof TSModule) {
			visitReference((TSModule) module.getParent());
			builder.append(".");
		}
		builder.append(module.getName());
	}

	public void visitReference(TSType type) {
		if (type instanceof TSParameterizedType) {
			TSParameterizedType paramType = (TSParameterizedType) type;
			visitReference(paramType.getBaseType());
			builder.append("<");
			List<TSType> parameters = paramType.getParameters();
			for (int i = 0; i < parameters.size(); i++) {
				if (i > 0) {
					builder.append(", ");
				}
				visitReference(parameters.get(i));
			}
			builder.append(">");
		}
		else {
			if (type.getParent() instanceof TSModule) {
				visitReference((TSModule) type.getParent());
				builder.append(".");
			}
			builder.append(type.getName());
		}

	}

	@Override
	public void visit(TSField element) {
		appendLine();
		builder.append(element.getName());
		if (element.isNullable()) {
			builder.append("?");
		}
		builder.append(": ");
		visitReference(element.getType());
		if (element.getInitializer() != null) {
			builder.append(" = ");
			builder.append(element.getInitializer());
		}
		builder.append(";");
	}

	@Override
	public void visit(TSIndexSignature element) {
		appendLine();
		builder.append("[key: ");
		visitReference(element.getKeyType());
		builder.append("]: ");
		visitReference(element.getValueType());
		builder.append(";");
	}

	@Override
	public void visit(TSArrayType element) {
		builder.append("Array<");
		element.accept(this);
		builder.append(">");
	}

	@Override
	public void visit(TSEnumType element) {
		// we make use of string literal types since enums are number-based in Typescript
		appendLine();
		appendExported(element);
		builder.append("type ");
		builder.append(element.getName());
		builder.append(" = ");

		List<TSEnumLiteral> literals = element.getLiterals();
		for (int i = 0; i < literals.size(); i++) {
			if (i > 0) {
				builder.append(" | ");
			}
			TSEnumLiteral literal = literals.get(i);
			builder.append('\"');
			builder.append(literal.getValue());
			builder.append('\"');
		}
		builder.append(";");
	}

	@Override
	public void visit(TSPrimitiveType element) {
		builder.append(element.getName());
	}

	@Override
	public void visit(TSMember element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void visit(TSAny tsAny) {
		builder.append(tsAny.getName());
	}

	@Override
	public String toString() {
		return builder.toString();
	}

	private void startScope() {
		builder.append("{");
		incIndentation();
	}

	private void incIndentation() {
		currentIndentation++;
	}

	private void endScope() {
		decIndentation();
		appendLine();
		builder.append("}");
	}

	private void decIndentation() {
		currentIndentation--;
	}

	private void appendLine() {
		builder.append(codeStyle.getLineSeparator());
		for (int i = 0; i < currentIndentation; i++) {
			builder.append(codeStyle.getIndentation());
		}
	}

	@Override
	public void accept(TSModule module) {
		appendLine();
		appendExported(module);
		builder.append("module ");
		builder.append(module.getName());
		startScope();
		for (TSElement type : module.getElements()) {
			type.accept(this);
		}
		endScope();
	}

	@Override
	public void accept(TSSource source) {
		for (TSImport importElement : source.getImports()) {
			importElement.accept(this);
		}

		for (TSExport exportElement : source.getExports()) {
			exportElement.accept(this);
		}

		for (TSElement element : source.getElements()) {
			element.accept(this);
		}
	}

	@Override
	public void visit(TSImport importElement) {
		builder.append("import {");

		Iterator<String> iterator = importElement.getTypeNames().iterator();
		while (iterator.hasNext()) {
			builder.append(iterator.next());
			if (iterator.hasNext()) {
				builder.append(", ");
			}
		}
		builder.append("} from '");
		builder.append(importElement.getPath());
		builder.append("'");
		builder.append(codeStyle.getLineSeparator());
	}

	@Override
	public void visit(TSExport exportElement) {
		builder.append("export ");
		if (exportElement.getAny()) {
			builder.append("*");
		}
		else {
			builder.append("{");
			Iterator<String> iterator = exportElement.getTypeNames().iterator();
			while (iterator.hasNext()) {
				builder.append(iterator.next());
				if (iterator.hasNext()) {
					builder.append(", ");
				}
			}
			builder.append("}");
		}
		builder.append(" from '");
		builder.append(exportElement.getPath());
		builder.append("'");
		builder.append(codeStyle.getLineSeparator());
	}

	@Override
	public void visit(TSParameterizedType parameterizedType) {
		throw new UnsupportedOperationException();
	}
}
