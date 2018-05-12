package io.crnk.gen.typescript.processor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.crnk.gen.typescript.model.TSContainerElement;
import io.crnk.gen.typescript.model.TSElement;
import io.crnk.gen.typescript.model.TSField;
import io.crnk.gen.typescript.model.TSFunction;
import io.crnk.gen.typescript.model.TSInterfaceType;
import io.crnk.gen.typescript.model.TSMember;
import io.crnk.gen.typescript.model.TSParameter;
import io.crnk.gen.typescript.model.TSParameterizedType;
import io.crnk.gen.typescript.model.TSPrimitiveType;
import io.crnk.gen.typescript.model.TSSource;
import io.crnk.gen.typescript.model.TSVisitorBase;
import io.crnk.gen.typescript.model.libraries.NgrxJsonApiLibrary;
import io.crnk.gen.typescript.transform.TSMetaDataObjectTransformation;


/**
 * Computes for each resource a factory method to create an empty object. The empty object ensures
 * that the basic data structures like 'attributes', 'relationships' and data within relationships are
 * initialized.
 */
public class TSEmptyObjectFactoryProcessor implements TSSourceProcessor {

	private static final String ATTRIBUTES_ATTRIBUTE = "attributes";

	private static final String RELATIONSHIPS_ATTRIBUTE = "relationships";

	@Override
	public List<TSSource> process(List<TSSource> sources) {
		EmptyObjectVisitor visitor = new EmptyObjectVisitor();
		for (TSSource source : sources) {
			source.accept(visitor);
		}
		return sources;
	}

	class EmptyObjectVisitor extends TSVisitorBase {

		private Map<TSElement, TSFunction> translationCache = new HashMap<>();

		@Override
		public void visit(TSInterfaceType interfaceType) {
			boolean doGenerate = interfaceType.implementsInterface(NgrxJsonApiLibrary.STORE_RESOURCE);
			if (doGenerate) {
				generate(interfaceType);
			}
		}

		private void generate(TSInterfaceType interfaceType) {
			if (translationCache.containsKey(interfaceType)) {
				translationCache.get(interfaceType);
			}

			String resourceType = interfaceType.getPrivateData(TSMetaDataObjectTransformation.PRIVATE_DATA_RESOURCE_TYPE,
					String.class);
			if (resourceType != null) {

				String name = "createEmpty" + interfaceType.getName();
				TSContainerElement parent = (TSContainerElement) interfaceType.getParent();

				List<String> body = buildFactoryBody(interfaceType, resourceType);

				TSFunction factoryMethod = new TSFunction();
				factoryMethod.setName(name);
				factoryMethod.setExported(true);
				factoryMethod.setParent(parent);
				factoryMethod.getStatements().addAll(body);

				factoryMethod.setType(interfaceType);
				factoryMethod.addParameter(new TSParameter("id", TSPrimitiveType.STRING, false));

				translationCache.put(interfaceType, factoryMethod);
				parent.addElement(factoryMethod);
			}
		}

		private List<String> buildFactoryBody(TSInterfaceType interfaceType, String resourceType) {
			TSMember attributesAttr = interfaceType.getDeclaredMember(ATTRIBUTES_ATTRIBUTE);
			TSMember relationshipsAttr = interfaceType.getDeclaredMember(RELATIONSHIPS_ATTRIBUTE);

			StringBuilder builder = new StringBuilder();
			builder.append("return {\n");
			builder.append("id: id,\n");
			builder.append("type: '" + resourceType + "',\n");

			if (attributesAttr != null) {
				builder.append("attributes: {\n");
				builder.append("},\n");
			}
			if (relationshipsAttr != null) {
				builder.append("relationships: {\n");
				TSInterfaceType relationshipsType = relationshipsAttr.getType().asInterfaceType();
				List<TSMember> relationshipMembers = relationshipsType.getMembers();
				for (TSMember member : relationshipMembers) {
					if (member.isField()) {
						TSField field = member.asField();

						TSParameterizedType fieldType = (TSParameterizedType) field.getType();
						boolean isArray = fieldType.getBaseType() == NgrxJsonApiLibrary.TYPED_MANY_RESOURCE_RELATIONSHIP;
						builder.append(field.getName() + ": {data: " + (isArray ? "[]" : "null") + "},\n");
					}
				}
				builder.append("},\n");
			}
			builder.append("};");
			return Arrays.asList(builder.toString());
		}
	}
}