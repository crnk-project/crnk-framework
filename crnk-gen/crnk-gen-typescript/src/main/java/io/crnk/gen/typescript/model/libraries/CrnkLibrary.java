package io.crnk.gen.typescript.model.libraries;

import io.crnk.gen.typescript.model.TSClassType;
import io.crnk.gen.typescript.model.TSPrimitiveType;
import io.crnk.gen.typescript.model.TSSource;
import io.crnk.gen.typescript.model.TSType;

public class CrnkLibrary {

	public static final TSSource EXPRESSION_SOURCE = new TSSource();

	public static final TSSource STUB_SOURCE = new TSSource();

	public static final TSClassType BEAN_PATH;

	public static final TSClassType STRING_PATH;

	public static final TSClassType NUMBER_PATH;

	public static final TSClassType BOOLEAN_PATH;

	public static final TSClassType QTYPED_ONE_RESOURCE_RELATIONSHIP;

	public static final TSClassType QTYPED_MANY_RESOURCE_RELATIONSHIP;

	public static final TSClassType ARRAY_PATH;

	public static final TSClassType MAP_PATH;

	static {
		EXPRESSION_SOURCE.setNpmPackage("@crnk/angular-ngrx");
		EXPRESSION_SOURCE.setDirectory("expression");
		STUB_SOURCE.setNpmPackage("@crnk/angular-ngrx");
		STUB_SOURCE.setDirectory("stub");

		BEAN_PATH = new TSClassType();
		BEAN_PATH.setName("BeanPath");
		BEAN_PATH.setParent(EXPRESSION_SOURCE);

		STRING_PATH = new TSClassType();
		STRING_PATH.setName("StringPath");
		STRING_PATH.setParent(EXPRESSION_SOURCE);

		NUMBER_PATH = new TSClassType();
		NUMBER_PATH.setName("NumberPath");
		NUMBER_PATH.setParent(EXPRESSION_SOURCE);

		BOOLEAN_PATH = new TSClassType();
		BOOLEAN_PATH.setName("BooleanPath");
		BOOLEAN_PATH.setParent(EXPRESSION_SOURCE);

		ARRAY_PATH = new TSClassType();
		ARRAY_PATH.setName("ArrayPath");
		ARRAY_PATH.setParent(EXPRESSION_SOURCE);

		MAP_PATH = new TSClassType();
		MAP_PATH.setName("MapPath");
		MAP_PATH.setParent(EXPRESSION_SOURCE);

		QTYPED_ONE_RESOURCE_RELATIONSHIP = new TSClassType();
		QTYPED_ONE_RESOURCE_RELATIONSHIP.setName("QTypedOneResourceRelationship");
		QTYPED_ONE_RESOURCE_RELATIONSHIP.setParent(STUB_SOURCE);

		QTYPED_MANY_RESOURCE_RELATIONSHIP = new TSClassType();
		QTYPED_MANY_RESOURCE_RELATIONSHIP.setName("QTypedManyResourceRelationship");
		QTYPED_MANY_RESOURCE_RELATIONSHIP.setParent(STUB_SOURCE);
	}

	private CrnkLibrary() {
	}

	public static TSType getPrimitiveExpression(String primitiveName) {
		if (TSPrimitiveType.STRING.getName().equalsIgnoreCase(primitiveName)) {
			return STRING_PATH;
		}
		if (TSPrimitiveType.BOOLEAN.getName().equalsIgnoreCase(primitiveName)) {
			return BOOLEAN_PATH;
		}
		if (TSPrimitiveType.NUMBER.getName().equalsIgnoreCase(primitiveName)) {
			return NUMBER_PATH;
		}
		throw new IllegalStateException(primitiveName);
	}

	/**
	 * Set-up without dependency to deprecated crnk npm library.
	 */
	public static void initNgrx(String packageName) {
		EXPRESSION_SOURCE.setName("crnk");
		EXPRESSION_SOURCE.setDirectory(null);
		EXPRESSION_SOURCE.setNpmPackage(packageName);

		STUB_SOURCE.setName("crnk");
		STUB_SOURCE.setDirectory(null);
		STUB_SOURCE.setNpmPackage(packageName);
	}

	@Deprecated
	public static void initCrnk() {
		EXPRESSION_SOURCE.setNpmPackage("@crnk/angular-ngrx");
		EXPRESSION_SOURCE.setDirectory("expression");
		EXPRESSION_SOURCE.setName(null);
		STUB_SOURCE.setNpmPackage("@crnk/angular-ngrx");
		STUB_SOURCE.setDirectory("stub");
		STUB_SOURCE.setName(null);
	}
}
