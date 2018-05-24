package io.crnk.gen.typescript.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.crnk.gen.typescript.internal.TypescriptUtils;
import io.crnk.gen.typescript.model.TSArrayType;
import io.crnk.gen.typescript.model.TSClassType;
import io.crnk.gen.typescript.model.TSContainerElement;
import io.crnk.gen.typescript.model.TSElement;
import io.crnk.gen.typescript.model.TSEnumType;
import io.crnk.gen.typescript.model.TSField;
import io.crnk.gen.typescript.model.TSFunction;
import io.crnk.gen.typescript.model.TSFunctionType;
import io.crnk.gen.typescript.model.TSIndexSignatureType;
import io.crnk.gen.typescript.model.TSInterfaceType;
import io.crnk.gen.typescript.model.TSMember;
import io.crnk.gen.typescript.model.TSModule;
import io.crnk.gen.typescript.model.TSParameterizedType;
import io.crnk.gen.typescript.model.TSPrimitiveType;
import io.crnk.gen.typescript.model.TSSource;
import io.crnk.gen.typescript.model.TSType;
import io.crnk.gen.typescript.model.TSVisitorBase;
import io.crnk.gen.typescript.model.libraries.CrnkLibrary;
import io.crnk.gen.typescript.model.libraries.NgrxJsonApiLibrary;
import io.crnk.gen.typescript.transform.TSMetaDataObjectTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Computes Type-safe query classes similar to QueryDSL for resource types.
 */
public class TSExpressionObjectProcessor implements TSSourceProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(TSExpressionObjectProcessor.class);

	@Override
	public List<TSSource> process(List<TSSource> sources) {
		QueryObjectVisitor visitor = new QueryObjectVisitor();
		for (TSSource source : sources) {
			source.accept(visitor);
		}
		return sources;
	}

	class QueryObjectVisitor extends TSVisitorBase {

		private Map<TSElement, TSClassType> translationMap = new HashMap<>();

		@Override
		public void visit(TSInterfaceType interfaceType) {
			boolean doGenerate = interfaceType.implementsInterface(NgrxJsonApiLibrary.STORE_RESOURCE);
			if (doGenerate) {
				generate(interfaceType);
			}
		}

		private TSClassType generate(TSInterfaceType interfaceType) {
			if (translationMap.containsKey(interfaceType)) {
				return translationMap.get(interfaceType);
			}

			String name = "Q" + interfaceType.getName();
			TSContainerElement parent = (TSContainerElement) interfaceType.getParent();

			TSClassType queryType = new TSClassType();
			queryType.setName(name);
			queryType.setExported(true);
			queryType.setParent(parent);
			queryType.setSuperType(new TSParameterizedType(CrnkLibrary.BEAN_PATH, interfaceType));
			translationMap.put(interfaceType, queryType);

			String metaElementId =
					interfaceType.getPrivateData(TSMetaDataObjectTransformation.PRIVATE_DATA_META_ELEMENT_ID, String.class);
			if (metaElementId != null) {
				TSField metaField = new TSField();
				metaField.setName("metaId");
				metaField.setType(TSPrimitiveType.STRING);
				metaField.setInitializer("'" + metaElementId + "'");
				queryType.addDeclaredMember(metaField);
			}

			if (parent instanceof TSSource) {

				TSModule module = TypescriptUtils.getModule(parent, queryType.getName(), -1, false);

				// class must come before module according to Typescript
				List<TSElement> elements = parent.getElements();
				int insertIndex = module != null ? elements.indexOf(module) : elements.size();
				parent.addElement(insertIndex, queryType);
			} else {
				TSModule module = (TSModule) parent;
				TSContainerElement grandParent = (TSContainerElement) module.getParent();

				// module added to end of file
				int insertionIndex = grandParent.getElements().size();
				TSModule queryModule = TypescriptUtils.getModule(grandParent, "Q" + module.getName(), insertionIndex, true);
				queryModule.addElement(queryType);
			}

			List<TSField> fields = new ArrayList<>(interfaceType.getFields());

			boolean isResource = interfaceType.implementsInterface(NgrxJsonApiLibrary.STORE_RESOURCE);
			if (isResource) {
				TSField idField = new TSField();
				idField.setName("id");
				idField.setType(TSPrimitiveType.STRING);
				fields.add(0, idField);

				TSField typeField = new TSField();
				typeField.setName("type");
				typeField.setType(TSPrimitiveType.STRING);
				fields.add(1, typeField);
			}

			for (TSField field : fields) {
				List<TSMember> members = setupField(queryType, field.getName(), field.getType());
				if (members != null) {
					members.forEach(member -> queryType.addDeclaredMember(member));
				} else {
					LOGGER.warn("query object generation for {}.{} not yet supported", interfaceType.getName(), field.getName());
				}
			}
			return queryType;
		}

		private List<TSMember> setupField(TSClassType queryType, String fieldName, TSType fieldType) {
			if (fieldType instanceof TSArrayType) {
				TSArrayType arrayType = (TSArrayType) fieldType;
				TSType valueType = arrayType.getElementType();
				if(valueType instanceof TSPrimitiveType) {
					TSField elementField = getField(setupField(queryType, fieldName, arrayType.getElementType()));
					TSType elementType = elementField.getType();
					TSPrimitiveType primitiveValueType = (TSPrimitiveType) valueType;

					TSField qField = new TSField();
					qField.setName(fieldName);
					qField.setType(new TSParameterizedType(CrnkLibrary.ARRAY_PATH, elementType, primitiveValueType));
					qField.setInitializer("new ArrayPath(this, '" + fieldName + "', " + elementType.getName() + ")");
					return Arrays.asList(qField);
				}return null;
			} else if (fieldType instanceof TSIndexSignatureType) {
				TSIndexSignatureType mapType = (TSIndexSignatureType) fieldType;
				TSType valueType = mapType.getValueType();
				if(valueType instanceof TSPrimitiveType) {
					TSField elementField = getField(setupField(queryType, fieldName, mapType.getValueType()));
					TSType elementType = elementField.getType();
					TSPrimitiveType primitiveValueType = (TSPrimitiveType) valueType;

					TSField qField = new TSField();
					qField.setName(fieldName);
					qField.setType(new TSParameterizedType(CrnkLibrary.MAP_PATH, elementType, primitiveValueType));
					qField.setInitializer("new MapPath(this, '" + fieldName + "', " + elementType.getName() + ")");
					return Arrays.asList(qField);
				}
				return null;
			} else if (fieldType instanceof TSPrimitiveType) {
				TSPrimitiveType primitiveFieldType = (TSPrimitiveType) fieldType;
				String primitiveName = TypescriptUtils.firstToUpper(primitiveFieldType.getName());

				TSField qField = new TSField();
				qField.setName(fieldName);
				qField.setType(CrnkLibrary.getPrimitiveExpression(primitiveName));
				qField.setInitializer(setupPrimitiveField(primitiveName, fieldName));
				return Arrays.asList(qField);
			} else if (fieldType instanceof TSEnumType) {
				TSField qField = new TSField();
				qField.setName(fieldName);
				qField.setType(CrnkLibrary.STRING_PATH);
				qField.setInitializer(setupPrimitiveField("String", fieldName));
				return Arrays.asList(qField);
			} else if (fieldType instanceof TSInterfaceType) {
				return setupInterfaceField(fieldName, fieldType);
			} else if (isRelationship(fieldType)) {
				return setupRelationshipField(queryType, fieldName, fieldType);
			} else {
				return null;
			}

		}

		private List<TSMember> setupRelationshipField(TSClassType queryType, String fieldName, TSType fieldType) {
			TSParameterizedType parameterizedType = (TSParameterizedType) fieldType;

			TSType baseType = parameterizedType.getBaseType();
			boolean many = baseType == NgrxJsonApiLibrary.TYPED_MANY_RESOURCE_RELATIONSHIP;
			TSType qbaseType = many ? CrnkLibrary.QTYPED_MANY_RESOURCE_RELATIONSHIP : CrnkLibrary.QTYPED_ONE_RESOURCE_RELATIONSHIP;

			List<TSType> parameters = parameterizedType.getParameters();
			if (parameters.size() == 1 && parameters.get(0) instanceof TSInterfaceType) {

				TSInterfaceType elementType = (TSInterfaceType) parameters.get(0);
				TSType qElementType = generate(elementType);
				if (qElementType == null) {
					throw new IllegalStateException("failed to convert " + fieldType);
				}

				StringBuilder initializer = new StringBuilder();
				initializer.append("new ");

				initializer.append(qbaseType.getName());
				initializer.append('<');
				if (qElementType.getParent() instanceof TSModule) {
					initializer.append(((TSModule) qElementType.getParent()).getName());
					initializer.append('.');
				}
				initializer.append(qElementType.getName());
				initializer.append(", ");
				initializer.append(elementType.getName());
				initializer.append('>');

				initializer.append("(this, \'");
				initializer.append(fieldName);
				initializer.append("\'");
				initializer.append(", " + qElementType.getName());
				initializer.append(")");

				String fieldAccess = "this._" + fieldName;
				TSFunction getterFunction = new TSFunction();
				getterFunction.setFunctionType(TSFunctionType.GETTER);
				getterFunction.setType(new TSParameterizedType(qbaseType, qElementType, elementType));
				getterFunction.setName(fieldName);
				List<String> statements = getterFunction.getStatements();
				statements.add("if (!" + fieldAccess + ") {\n" + fieldAccess + " =\n\t" + initializer.toString() + ";" + "\n}");
				statements.add("return " + fieldAccess + ";");

				TSField qField = new TSField();
				qField.setName("_" + fieldName);
				qField.setType(new TSParameterizedType(qbaseType, qElementType, elementType));
				qField.setPrivate(true);

				return Arrays.asList(qField, getterFunction);
			} else {
				return null;
			}
		}

		private boolean isRelationship(TSType fieldType) {
			if (!(fieldType instanceof TSParameterizedType)) {
				return false;
			}
			TSParameterizedType paramType = (TSParameterizedType) fieldType;
			return paramType.getBaseType() == NgrxJsonApiLibrary.TYPED_ONE_RESOURCE_RELATIONSHIP
					|| paramType.getBaseType() == NgrxJsonApiLibrary.TYPED_MANY_RESOURCE_RELATIONSHIP;
		}

		private String setupPrimitiveField(String primitiveName, String fieldName) {
			StringBuilder initializer = new StringBuilder();
			initializer.append("this.create");
			initializer.append(primitiveName);
			initializer.append("(\'");
			initializer.append(fieldName);
			initializer.append("')");
			return initializer.toString();
		}

		private List<TSMember> setupInterfaceField(String fieldName, TSType fieldType) {
			StringBuilder initializer = new StringBuilder();

			TSType qFieldType = generate((TSInterfaceType) fieldType);
			if (qFieldType == null) {
				throw new IllegalStateException("failed to convert " + fieldType);
			}

			initializer.append("new ");
			if (qFieldType.getParent() instanceof TSModule) {
				initializer.append(((TSModule) qFieldType.getParent()).getName());
				initializer.append('.');
			}
			initializer.append(qFieldType.getName());
			initializer.append("(this, \'");
			initializer.append(fieldName);
			initializer.append("\')");

			TSField qField = new TSField();
			qField.setName(fieldName);
			qField.setInitializer(initializer.toString());
			qField.setType(qFieldType);
			return Arrays.asList(qField);
		}
	}

	private TSField getField(List<TSMember> members) {
		return (TSField) members.stream().filter(it -> it instanceof TSField).findFirst().get();
	}
}
