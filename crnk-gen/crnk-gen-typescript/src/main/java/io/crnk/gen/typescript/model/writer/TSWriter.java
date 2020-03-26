package io.crnk.gen.typescript.model.writer;

import io.crnk.gen.typescript.model.TSAny;
import io.crnk.gen.typescript.model.TSArrayType;
import io.crnk.gen.typescript.model.TSClassType;
import io.crnk.gen.typescript.model.TSElement;
import io.crnk.gen.typescript.model.TSEnumLiteral;
import io.crnk.gen.typescript.model.TSEnumType;
import io.crnk.gen.typescript.model.TSExport;
import io.crnk.gen.typescript.model.TSExportedElement;
import io.crnk.gen.typescript.model.TSField;
import io.crnk.gen.typescript.model.TSFunction;
import io.crnk.gen.typescript.model.TSFunctionType;
import io.crnk.gen.typescript.model.TSImport;
import io.crnk.gen.typescript.model.TSIndexSignatureType;
import io.crnk.gen.typescript.model.TSInterfaceType;
import io.crnk.gen.typescript.model.TSMember;
import io.crnk.gen.typescript.model.TSModule;
import io.crnk.gen.typescript.model.TSParameter;
import io.crnk.gen.typescript.model.TSParameterizedType;
import io.crnk.gen.typescript.model.TSPrimitiveType;
import io.crnk.gen.typescript.model.TSSource;
import io.crnk.gen.typescript.model.TSType;
import io.crnk.gen.typescript.model.TSVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
			appendLine();
			element.getIndexSignature().accept(this);
			builder.append(";");
		}

		for (TSMember member : element.getDeclaredMembers()) {
			member.accept(this);
		}
		endScope();
	}

	private void sortAndAppendTypes(Collection<TSInterfaceType> types) {
		List<TSInterfaceType> sorted = new ArrayList<>(types);
		Collections.sort(sorted, (type0, type1) -> type0.getName().compareTo
				(type1.getName()));


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
			appendLine();
			element.getIndexSignature().accept(this);
			builder.append(";");
		}

		for (TSMember member : element.getDeclaredMembers()) {
			member.accept(this);
		}
		endScope();
	}

	private void appendExported(TSExportedElement element) {
		if (element.isExported()) {
			builder.append("export ");
		}
	}

	private void visitReference(TSModule module) {
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
		} else if (type instanceof TSIndexSignatureType) {
			builder.append("{ ");
			visit((TSIndexSignatureType) type);
			builder.append(" }");
		} else {
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
		if (element.isPrivate()) {
			builder.append("private ");
		}
		builder.append(element.getName());
		if (element.isNullable()) {
			builder.append("?");
		}
		if (element.getInitializer() == null || !(element.getType() instanceof TSPrimitiveType)) {
			// primitive types can be trivially inferred and break tslint otherwise
			builder.append(": ");
			visitReference(element.getType());
		}
		if (element.getInitializer() != null) {
			builder.append(" = ");
			builder.append(element.getInitializer());
		}
		builder.append(";");
	}

	@Override
	public void visit(TSIndexSignatureType element) {
		builder.append("[key");
		TSType keyType = element.getKeyType();
		if (keyType instanceof TSEnumType) {
			builder.append(" in ");
		} else if (keyType instanceof TSPrimitiveType && ("string".equals(keyType.getName()) || "number".equals(keyType.getName()))) {
			builder.append(": ");
		} else {
			throw new UnsupportedOperationException();
		}
		visitReference(keyType);
		builder.append("]: ");
		visitReference(element.getValueType());
	}

	@Override
	public void visit(TSArrayType element) {
		builder.append("Array<");
		element.getElementType().accept(this);
		builder.append(">");
	}

	@Override
	public void visit(TSEnumType element) {
		// we make use of string literal types since enums are number-based in Typescript
		appendLine();
		appendExported(element);

		if (codeStyle.isStringEnums()) {
			builder.append("enum ");
			builder.append(element.getName());
			startScope();

			List<TSEnumLiteral> literals = element.getLiterals();
			for (int i = 0; i < literals.size(); i++) {
				appendLine();
				TSEnumLiteral literal = literals.get(i);
				builder.append(literal.getValue());
				builder.append(" = \'");
				builder.append(literal.getValue());
				builder.append('\'');
				builder.append(",");
			}

			endScope();
		} else {
			builder.append("type ");
			builder.append(element.getName());
			builder.append(" = ");

			List<TSEnumLiteral> literals = element.getLiterals();
			for (int i = 0; i < literals.size(); i++) {
				if (i > 0) {
					builder.append(" | ");
				}
				TSEnumLiteral literal = literals.get(i);
				builder.append('\'');
				builder.append(literal.getValue());
				builder.append('\'');
			}
			builder.append(";");
		}
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
		builder.append(" {");
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
		if (importElement.getTypeNames().size() == 1) {
			builder.append(iterator.next());
		} else {
			builder.append("\n");
			while (iterator.hasNext()) {
				builder.append("\t");
				builder.append(iterator.next());
				if (iterator.hasNext()) {
					builder.append(",");
				}
				builder.append("\n");
			}
		}

		builder.append("} from '");
		builder.append(importElement.getPath());
		builder.append("';");
		builder.append(codeStyle.getLineSeparator());
	}

	@Override
	public void visit(TSExport exportElement) {
		builder.append("export ");
		if (exportElement.getAny()) {
			builder.append("*");
		} else {
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
		builder.append("';");
		builder.append(codeStyle.getLineSeparator());
	}

	@Override
	public void visit(TSParameterizedType parameterizedType) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void visit(TSFunction function) {
		appendLine();

		if (function.getFunctionType() == TSFunctionType.GETTER) {
			if (function.isPrivate()) {
				builder.append("private ");
			}
			builder.append("get ");
			builder.append(function.getName());
			builder.append("()");
		} else {
			appendExported(function);
			builder.append("let ");
			builder.append(function.getName());
			builder.append(" = function(");
			Iterator<TSParameter> iterator = function.getParameters().iterator();
			while (iterator.hasNext()) {
				TSParameter parameter = iterator.next();

				builder.append(parameter.getName());
				builder.append(": ");
				visitReference(parameter.getType());
				if (iterator.hasNext()) {
					builder.append(", ");
				}
			}
			builder.append(")");
		}

		if (function.getType() != null) {
			builder.append(": ");
			visitReference(function.getType());
		}
		startScope();
		for (String statement : function.getStatements()) {
			writeStatement(statement);
		}
		endScope();
		builder.append(";");
	}

	private void writeStatement(String statement) {
		String[] lines = statement.split("\\n");
		for (String line : lines) {
			if (line.startsWith("}")) {
				decIndentation();
			}

			appendLine();
			builder.append(line);

			if (line.endsWith("{")) {
				incIndentation();
			}
		}
	}

	@Override
	public void visit(TSParameter parameter) {

	}
}
