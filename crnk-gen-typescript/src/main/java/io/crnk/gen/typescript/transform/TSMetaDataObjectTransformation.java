package io.crnk.gen.typescript.transform;

import java.util.Set;

import io.crnk.gen.typescript.TypescriptUtils;
import io.crnk.gen.typescript.model.TSContainerElement;
import io.crnk.gen.typescript.model.TSElement;
import io.crnk.gen.typescript.model.TSField;
import io.crnk.gen.typescript.model.TSIndexSignature;
import io.crnk.gen.typescript.model.TSInterfaceType;
import io.crnk.gen.typescript.model.TSModule;
import io.crnk.gen.typescript.model.TSParameterizedType;
import io.crnk.gen.typescript.model.TSPrimitiveType;
import io.crnk.gen.typescript.model.TSSource;
import io.crnk.gen.typescript.model.TSType;
import io.crnk.gen.typescript.model.libraries.NgrxJsonApiLibrary;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaKey;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;

public class TSMetaDataObjectTransformation implements TSMetaTransformation {

	private static final String ATTRIBUTES_CLASS_NAME = "Attributes";

	private static final String RELATIONSHIPS_CLASS_NAME = "Relationships";


	@Override
	public boolean accepts(MetaElement element) {
		return element instanceof MetaDataObject;
	}

	@Override
	public TSElement transform(MetaElement element, TSMetaTransformationContext context) {
		MetaDataObject metaDataObject = (MetaDataObject) element;

		TSInterfaceType interfaceType = new TSInterfaceType();
		interfaceType.setName(metaDataObject.getName());
		interfaceType.setExported(true);

		context.putMapping(metaDataObject, interfaceType);

		setupParent(context, interfaceType, metaDataObject);

		if (generateAsResource(metaDataObject)) {
			interfaceType.getImplementedInterfaces().add(NgrxJsonApiLibrary.STORE_RESOURCE);
			generateResourceFields(context, interfaceType, metaDataObject);
		}
		else {
			if (metaDataObject.getSuperType() != null) {
				TSInterfaceType superInterface = (TSInterfaceType) context.transform(metaDataObject.getSuperType());
				interfaceType.getImplementedInterfaces().add(superInterface);
			}
			for (MetaDataObject interfaceMeta : metaDataObject.getInterfaces()) {
				TSInterfaceType implementedinterfaceType = (TSInterfaceType) context.transform(interfaceMeta);
				interfaceType.getImplementedInterfaces().add(implementedinterfaceType);
			}

			generateAttributes(context, interfaceType, metaDataObject);
		}

		for (MetaDataObject subType : metaDataObject.getSubTypes()) {
			context.transform(subType);
		}

		return interfaceType;
	}

	/**
	 * Generate resources and their base classes as resources.
	 */
	private static boolean generateAsResource(MetaDataObject metaDataObject) {
		if (metaDataObject instanceof MetaResource) {
			return true;
		}
		Set<MetaDataObject> subTypes = metaDataObject.getSubTypes();
		if (!subTypes.isEmpty()) {
			for (MetaDataObject subType : metaDataObject.getSubTypes()) {
				if (!generateAsResource(subType)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private static void generateResourceFields(TSMetaTransformationContext context, TSInterfaceType interfaceType, MetaDataObject meta) {
		TSInterfaceType attributesType = new TSInterfaceType();
		attributesType.setName(ATTRIBUTES_CLASS_NAME);
		attributesType.setExported(true);

		TSInterfaceType relationshipsType = new TSInterfaceType();
		relationshipsType.setName(RELATIONSHIPS_CLASS_NAME);
		relationshipsType.setExported(true);
		TSIndexSignature relationshipsIndexSignature = new TSIndexSignature();
		relationshipsIndexSignature.setKeyType(TSPrimitiveType.STRING);
		relationshipsIndexSignature.setValueType(NgrxJsonApiLibrary.RESOURCE_RELATIONSHIP);
		relationshipsIndexSignature.setParent(relationshipsType);
		relationshipsType.setIndexSignature(relationshipsIndexSignature );

		MetaDataObject superType = meta.getSuperType();
		if (superType != null) {

			TSInterfaceType superInterface = (TSInterfaceType) context.transform(superType);
			interfaceType.getImplementedInterfaces().add(superInterface);

			TSInterfaceType superAttributeType = TypescriptUtils.getNestedInterface(superInterface, ATTRIBUTES_CLASS_NAME, false);
			if (superAttributeType != null) {
				attributesType.getImplementedInterfaces().add(superAttributeType);
			}

			TSInterfaceType superRelationshipType = TypescriptUtils.getNestedInterface(superInterface, RELATIONSHIPS_CLASS_NAME, false);
			if (superRelationshipType != null) {
				relationshipsType.getImplementedInterfaces().add(superRelationshipType);
			}
		}

		// TODO remo: interface support
		MetaKey primaryKey = meta.getPrimaryKey();
		for (MetaAttribute attr : meta.getDeclaredAttributes()) {
			if (primaryKey != null && primaryKey.getUniqueElement().equals(attr)) {
				continue;
			}
			generateResourceField(attr, context, interfaceType, attributesType, relationshipsType );
		}

		if (!isEmpty(relationshipsType)) {
			TSModule module = TypescriptUtils.getNestedTypeContainer(interfaceType, true);

			module.getElements().add(relationshipsType);
			relationshipsType.setParent(module);

			TSField relationshipsField = new TSField();
			relationshipsField.setName("relationships");
			relationshipsField.setType(relationshipsType);
			relationshipsField.setNullable(true);
			interfaceType.getMembers().add(relationshipsField);
		}
		if (!isEmpty(attributesType)) {
			TSModule module = TypescriptUtils.getNestedTypeContainer(interfaceType, true);

			module.getElements().add(attributesType);
			attributesType.setParent(module);

			TSField attributesField = new TSField();
			attributesField.setName("attributes");
			attributesField.setType(attributesType);
			attributesField.setNullable(true);
			interfaceType.getMembers().add(attributesField);
		}
	}

	private static void generateResourceField(MetaAttribute attr, TSMetaTransformationContext context, TSInterfaceType interfaceType, TSInterfaceType attributesType, TSInterfaceType relationshipsType) {
		MetaType metaElementType = attr.getType().getElementType();
		TSType elementType = (TSType) context.transform(metaElementType);

		TSField field = new TSField();
		field.setName(attr.getName());
		field.setType(elementType);
		field.setNullable(true);

		if (attr.isAssociation()) {
			TSType relationshipType = attr.getType().isCollection() ? NgrxJsonApiLibrary.TYPED_MANY_RESOURCE_RELATIONSHIP : NgrxJsonApiLibrary.TYPED_ONE_RESOURCE_RELATIONSHIP;
			field.setType(new TSParameterizedType(relationshipType, elementType));
			relationshipsType.getMembers().add(field);
			field.setParent(relationshipsType);
		}
		else if (attr instanceof MetaResourceField && ((MetaResourceField) attr).isMeta()) {
			field.setName("meta");
			interfaceType.getMembers().add(field);
			field.setParent(interfaceType);
		}
		else if (attr instanceof MetaResourceField && ((MetaResourceField) attr).isLinks()) {
			field.setName("links");
			interfaceType.getMembers().add(field);
			field.setParent(interfaceType);
		}
		else {
			attributesType.getMembers().add(field);
			field.setParent(attributesType);
		}
	}

	private static boolean isEmpty(TSInterfaceType type) {
		return type.getMembers().isEmpty() && type.getImplementedInterfaces().isEmpty();
	}

	private static void generateAttributes(TSMetaTransformationContext context, TSInterfaceType interfaceType, MetaDataObject element) {
		for (MetaAttribute attr : element.getDeclaredAttributes()) {
			MetaType elementType = attr.getType().getElementType();

			TSField field = new TSField();
			field.setName(attr.getName());
			field.setType((TSType) context.transform(elementType));
			field.setNullable(true);
			interfaceType.getMembers().add(field);
		}
	}

	private static void setupParent(TSMetaTransformationContext context, TSInterfaceType interfaceType, MetaDataObject metaDataObject) {
		TSContainerElement parent = null;

		// move links and meta information to the resource itself
		boolean isMeta = TypescriptUtils.isInstance(metaDataObject.getImplementationClass(), "io.crnk.core.resource.meta"
				+ ".MetaInformation");
		boolean isLinks = TypescriptUtils.isInstance(metaDataObject.getImplementationClass(), "io.crnk.core.resource.links"
				+ ".LinksInformation");
		if ((isMeta || isLinks) && metaDataObject.getImplementationClass().getEnclosingClass() != null) {
			MetaElement enclosingMeta = context.getMeta(metaDataObject.getImplementationClass().getEnclosingClass());
			if (enclosingMeta instanceof MetaResource) {
				TSType enclosingType = (TSType) context.transform(enclosingMeta);
				TSModule module = TypescriptUtils.getNestedTypeContainer(enclosingType, true);
				interfaceType.setName(isLinks ? "Links" : "Meta");
				parent = module;
			}
		}
		if (parent == null) {
			TSSource source = new TSSource();
			source.setName(TypescriptUtils.toFileName(metaDataObject.getName()));
			source.setNpmPackage(context.getNpmPackage(metaDataObject));
			source.setDirectory(context.getDirectory(metaDataObject));
			context.addSource(source);
			parent = source;
		}

		parent.getElements().add(interfaceType);
		interfaceType.setParent(parent);
	}

}
