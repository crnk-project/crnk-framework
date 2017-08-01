package io.crnk.gen.typescript.processor;

import io.crnk.gen.typescript.internal.TypescriptUtils;
import io.crnk.gen.typescript.model.*;
import io.crnk.gen.typescript.model.libraries.CrnkLibrary;
import io.crnk.gen.typescript.model.libraries.NgrxJsonApiLibrary;
import io.crnk.gen.typescript.transform.TSMetaDataObjectTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Computes Type-safe query classes similar to QueryDSL for resource types.
 */
public class TSExpressionObjectProcessor implements TSSourceProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(TSExpressionObjectProcessor.class);

	@Override
	public Set<TSSource> process(Set<TSSource> sources) {
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

			String metaElementId = interfaceType.getPrivateData(TSMetaDataObjectTransformation.PRIVATE_DATA_META_ELEMENT_ID, String.class);
			if (metaElementId != null) {
				TSField metaField = new TSField();
				metaField.setName("metaId");
				metaField.setType(TSPrimitiveType.STRING);
				metaField.setInitializer("'" + metaElementId + "'");
				queryType.addDeclaredMember(metaField);
			}

			if (interfaceType.getName().equals("MetaKey")) {
				System.out.println("Hallo");
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

			List<TSField> fields = interfaceType.getFields();
			for (TSField field : fields) {
				TSField qField = new TSField();
				qField.setName(field.getName());
				setupField(interfaceType, qField, field);
				if (qField.getInitializer() != null) {
					queryType.addDeclaredMember(qField);
				}
			}
			return queryType;
		}

		private void setupField(TSInterfaceType interfaceType, TSField qField, TSField field) {
			TSType fieldType = field.getType();
			if (fieldType instanceof TSPrimitiveType) {
				TSPrimitiveType primitiveFieldType = (TSPrimitiveType) fieldType;
				String primitiveName = TypescriptUtils.firstToUpper(primitiveFieldType.getName());
				qField.setType(CrnkLibrary.getPrimitiveExpression(primitiveName));
				qField.setInitializer(setupPrimitiveField(primitiveName, field));
			} else if (fieldType instanceof TSEnumType) {
				qField.setType(CrnkLibrary.STRING_EXPRESSION);
				qField.setInitializer(setupPrimitiveField("String", qField));
			} else if (fieldType instanceof TSInterfaceType) {
				setupInterfaceField(qField, field);
			} else if (isRelationship(fieldType)) {
				setupRelationshipField(interfaceType, qField, field);
			} else {
				LOGGER.warn("query object generation for {}.{} not yet supported", interfaceType.getName(), field.getName());
			}
		}

		private void setupRelationshipField(TSInterfaceType interfaceType, TSField qField, TSField field) {
			TSType fieldType = field.getType();
			TSParameterizedType parameterizedType = (TSParameterizedType) fieldType;

			TSType baseType = parameterizedType.getBaseType();
			TSType qbaseType = baseType == NgrxJsonApiLibrary.TYPED_MANY_RESOURCE_RELATIONSHIP
					? CrnkLibrary.QTYPED_MANY_RESOURCE_RELATIONSHIP : CrnkLibrary.QTYPED_ONE_RESOURCE_RELATIONSHIP;

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
				initializer.append(qField.getName());
				initializer.append("\'");
				//initializer.append(", new " + qElementType.getName() + "(null, 'data')");
				initializer.append(", " + qElementType.getName());
				initializer.append(")");
				qField.setInitializer(initializer.toString());
				qField.setType(new TSParameterizedType(qbaseType, qElementType, elementType));
			} else {
				LOGGER.warn("query object generation for {}.{} not yet supported", interfaceType.getName(), field.getName());
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

		private String setupPrimitiveField(String primitiveName, TSField qField) {
			StringBuilder initializer = new StringBuilder();
			initializer.append("this.create");
			initializer.append(primitiveName);
			initializer.append("(\'");
			initializer.append(qField.getName());
			initializer.append("')");
			return initializer.toString();
		}

		private void setupInterfaceField(TSField qField, TSField field) {
			TSType fieldType = field.getType();
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
			initializer.append(qField.getName());
			initializer.append("\')");
			qField.setInitializer(initializer.toString());
			qField.setType(qFieldType);
		}
	}
}
