package io.crnk.gen.typescript.model.libraries;

import io.crnk.gen.typescript.model.TSClassType;
import io.crnk.gen.typescript.model.TSPrimitiveType;
import io.crnk.gen.typescript.model.TSSource;
import io.crnk.gen.typescript.model.TSType;

public class ExpressionLibrary {

	public static final TSClassType BEAN_PATH;

	public static final TSClassType STRING_EXPRESSION;

	public static final TSClassType NUMBER_EXPRESSION;

	public static final TSClassType BOOLEAN_EXPRESSION;

	public static final TSClassType QTYPED_ONE_RESOURCE_RELATIONSHIP;

	public static final TSClassType QTYPED_MANY_RESOURCE_RELATIONSHIP;

	static {
		TSSource ngrxJsonApiSource = new TSSource();
		ngrxJsonApiSource.setNpmPackage("@crnk/binding");

		BEAN_PATH = new TSClassType();
		BEAN_PATH.setName("BeanPath");
		BEAN_PATH.setParent(ngrxJsonApiSource);

		STRING_EXPRESSION = new TSClassType();
		STRING_EXPRESSION.setName("StringExpression");
		STRING_EXPRESSION.setParent(ngrxJsonApiSource);

		NUMBER_EXPRESSION = new TSClassType();
		NUMBER_EXPRESSION.setName("NumberExpression");
		NUMBER_EXPRESSION.setParent(ngrxJsonApiSource);

		BOOLEAN_EXPRESSION = new TSClassType();
		BOOLEAN_EXPRESSION.setName("BooleanExpression");
		BOOLEAN_EXPRESSION.setParent(ngrxJsonApiSource);

		QTYPED_ONE_RESOURCE_RELATIONSHIP = new TSClassType();
		QTYPED_ONE_RESOURCE_RELATIONSHIP.setName("QTypedOneResourceRelationship");
		QTYPED_ONE_RESOURCE_RELATIONSHIP.setParent(ngrxJsonApiSource);

		QTYPED_MANY_RESOURCE_RELATIONSHIP = new TSClassType();
		QTYPED_MANY_RESOURCE_RELATIONSHIP.setName("QTypedManyResourceRelationship");
		QTYPED_MANY_RESOURCE_RELATIONSHIP.setParent(ngrxJsonApiSource);
	}

	private ExpressionLibrary() {
	}

	public static TSType getExpression(String primitiveName) {
		if (TSPrimitiveType.STRING.getName().equalsIgnoreCase(primitiveName)) {
			return STRING_EXPRESSION;
		}
		if (TSPrimitiveType.BOOLEAN.getName().equalsIgnoreCase(primitiveName)) {
			return BOOLEAN_EXPRESSION;
		}
		if (TSPrimitiveType.NUMBER.getName().equalsIgnoreCase(primitiveName)) {
			return NUMBER_EXPRESSION;
		}
		throw new IllegalStateException(primitiveName);
	}
}
