package io.crnk.gen.typescript.transform;

import java.util.List;

import io.crnk.gen.typescript.internal.TypescriptUtils;
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

	public static final String PRIVATE_DATA_RESOURCE_TYPE = "resourceType";

	public static final String PRIVATE_DATA_META_ELEMENT_ID = "metaElement";


	@Override
	public boolean accepts(MetaElement element) {
		return element instanceof MetaDataObject;
	}

	@Override
	public TSElement transform(MetaElement element, TSMetaTransformationContext context, TSMetaTransformationOptions options) {
		MetaDataObject metaDataObject = (MetaDataObject) element;

		TSInterfaceType interfaceType = new TSInterfaceType();
		interfaceType.setName(metaDataObject.getName());
		interfaceType.setExported(true);

		if (metaDataObject instanceof MetaResource) {
			MetaResource metaResource = (MetaResource) metaDataObject;
			String resourceType = metaResource.getResourceType();
			interfaceType.setPrivateData(TSMetaDataObjectTransformation.PRIVATE_DATA_RESOURCE_TYPE, resourceType);
		}
		interfaceType.setPrivateData(TSMetaDataObjectTransformation.PRIVATE_DATA_META_ELEMENT_ID, element.getId());

		context.putMapping(metaDataObject, interfaceType);

		if (options.getParent() == null) {
			setupParent(context, interfaceType, metaDataObject);
		}
		else {
			options.getParent().addElement(interfaceType);
		}

		System.out.println(
				metaDataObject.getName() + " " + metaDataObject.getSuperType() + " " + generateAsResource(metaDataObject));

		if (generateAsResource(metaDataObject)) {
			if (metaDataObject.getSuperType() == null) {
				interfaceType.getImplementedInterfaces().add(NgrxJsonApiLibrary.STORE_RESOURCE);
			}
			else {
				// trigger generation of super type, fully attach during post processing
				MetaDataObject superType = metaDataObject.getSuperType();
				if(generateAsResource(superType)) {
					TSElement superInterface = context.transform(superType, TSMetaTransformationOptions.EMPTY);
					interfaceType.getImplementedInterfaces().add((TSInterfaceType) superInterface);
				}
			}


			generateResourceFields(context, interfaceType, metaDataObject);
		}
		else {
			if (metaDataObject.getSuperType() != null) {
				TSInterfaceType superInterface = (TSInterfaceType) context.transform(metaDataObject.getSuperType(),
						TSMetaTransformationOptions.EMPTY);
				interfaceType.getImplementedInterfaces().add(superInterface);
			}
			for (MetaDataObject interfaceMeta : metaDataObject.getInterfaces()) {
				TSInterfaceType implementedinterfaceType = (TSInterfaceType) context.transform(interfaceMeta,
						TSMetaTransformationOptions.EMPTY);
				interfaceType.getImplementedInterfaces().add(implementedinterfaceType);
			}


			generateAttributes(context, interfaceType, metaDataObject);
		}

		for (MetaDataObject subType : metaDataObject.getSubTypes()) {
			context.transform(subType, TSMetaTransformationOptions.EMPTY);
		}

		return interfaceType;
	}

	@Override
	public void postTransform(TSElement element, TSMetaTransformationContext context) {
		if (element instanceof TSInterfaceType) {
			TSInterfaceType interfaceType = (TSInterfaceType) element;
			String metaId =
					interfaceType.getPrivateData(TSMetaDataObjectTransformation.PRIVATE_DATA_META_ELEMENT_ID, String.class);
			MetaElement metaElement = context.getMeta(metaId);
			if (metaElement instanceof MetaDataObject) {

				MetaDataObject metaDataObject = (MetaDataObject) metaElement;
				if (generateAsResource(metaDataObject)) {
					// link to super type, only available once all fields are initialized
					MetaDataObject superType = metaDataObject.getSuperType();
					if (superType != null) {
						setAttributeSuperType(interfaceType, metaDataObject, context);
						setRelationshipsSuperType(interfaceType, metaDataObject, context);
					}
				}
			}
		}
	}

	private void setRelationshipsSuperType(TSInterfaceType interfaceType, MetaDataObject metaDataObject,
			TSMetaTransformationContext context) {
		// iterate over super types till (non-empty) one is found with a relationship interface definition
		MetaDataObject current = metaDataObject;
		while (current.getSuperType() != null && generateAsResource(current.getSuperType())) {
			MetaDataObject superType = current.getSuperType();
			TSInterfaceType superInterface = (TSInterfaceType) context.transform(superType, TSMetaTransformationOptions.EMPTY);

			TSInterfaceType relationshipsType =
					TypescriptUtils.getNestedInterface(interfaceType, RELATIONSHIPS_CLASS_NAME, false);
			if (relationshipsType != null) {
				TSInterfaceType superRelationshipType =
						TypescriptUtils.getNestedInterface(superInterface, RELATIONSHIPS_CLASS_NAME,
								false);
				if (superRelationshipType != null) {
					relationshipsType.getImplementedInterfaces().add(superRelationshipType);
					break;
				}
			}
			current = superType;
		}
	}

	private void setAttributeSuperType(TSInterfaceType interfaceType, MetaDataObject metaDataObject,
			TSMetaTransformationContext context) {
		// iterate over super types till (non-empty) one is found with a attributes interface definition
		MetaDataObject current = metaDataObject;
		while (current.getSuperType() != null && generateAsResource(current.getSuperType())) {
			MetaDataObject superType = current.getSuperType();
			TSInterfaceType superInterface = (TSInterfaceType) context.transform(superType, TSMetaTransformationOptions.EMPTY);

			TSInterfaceType attributesType = TypescriptUtils.getNestedInterface(interfaceType, ATTRIBUTES_CLASS_NAME, false);
			if (attributesType != null) {
				TSInterfaceType superAttributeType = TypescriptUtils.getNestedInterface(superInterface, ATTRIBUTES_CLASS_NAME,
						false);

				if (superAttributeType != null) {
					attributesType.getImplementedInterfaces().add(superAttributeType);
					break;
				}
			}
			current = superType;
		}
	}

	/**
	 * Generate resources and their base classes as resources.
	 */
	private static boolean generateAsResource(MetaDataObject metaDataObject) {
		if (metaDataObject instanceof MetaResource) {
			return true;
		}
		List<MetaDataObject> subTypes = metaDataObject.getSubTypes(true, false);
		if (!subTypes.isEmpty()) {
			for (MetaDataObject subType : subTypes) {
				if (generateAsResource(subType)) {
					return true;
				}
			}
			return false;
		}
		return false;
	}

	private static void generateResourceFields(TSMetaTransformationContext context, TSInterfaceType interfaceType,
			MetaDataObject meta) {
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
		relationshipsType.setIndexSignature(relationshipsIndexSignature);

		// TODO remo: interface support
		MetaKey primaryKey = meta.getPrimaryKey();
		for (MetaAttribute attr : meta.getDeclaredAttributes()) {
			if (primaryKey != null && primaryKey.getUniqueElement().equals(attr)) {
				continue;
			}
			generateResourceField(attr, context, interfaceType, attributesType, relationshipsType);
		}

		if (!isEmpty(relationshipsType)) {
			TSModule module = TypescriptUtils.getNestedTypeContainer(interfaceType, true);

			module.getElements().add(relationshipsType);
			relationshipsType.setParent(module);

			TSField relationshipsField = new TSField();
			relationshipsField.setName("relationships");
			relationshipsField.setType(relationshipsType);
			relationshipsField.setNullable(true);
			interfaceType.getDeclaredMembers().add(relationshipsField);
		}
		if (!isEmpty(attributesType)) {
			TSModule module = TypescriptUtils.getNestedTypeContainer(interfaceType, true);

			module.getElements().add(attributesType);
			attributesType.setParent(module);

			TSField attributesField = new TSField();
			attributesField.setName("attributes");
			attributesField.setType(attributesType);
			attributesField.setNullable(true);
			interfaceType.getDeclaredMembers().add(attributesField);
		}
	}

	private static void generateResourceField(MetaAttribute attr, TSMetaTransformationContext context,
			TSInterfaceType interfaceType, TSInterfaceType attributesType, TSInterfaceType relationshipsType) {
		MetaType metaElementType = attr.getType().getElementType();
		TSType elementType = (TSType) context.transform(metaElementType, TSMetaTransformationOptions.EMPTY);

		TSField field = new TSField();
		field.setName(attr.getName());
		field.setType(elementType);
		field.setNullable(true);

		if (attr.isAssociation()) {
			TSType relationshipType = attr.getType().isCollection() ? NgrxJsonApiLibrary.TYPED_MANY_RESOURCE_RELATIONSHIP
					: NgrxJsonApiLibrary.TYPED_ONE_RESOURCE_RELATIONSHIP;
			field.setType(new TSParameterizedType(relationshipType, elementType));
			relationshipsType.getDeclaredMembers().add(field);
			field.setParent(relationshipsType);
		}
		else if (attr instanceof MetaResourceField && ((MetaResourceField) attr).isMeta()) {
			field.setName("meta");
			interfaceType.getDeclaredMembers().add(field);
			field.setParent(interfaceType);
		}
		else if (attr instanceof MetaResourceField && ((MetaResourceField) attr).isLinks()) {
			field.setName("links");
			interfaceType.getDeclaredMembers().add(field);
			field.setParent(interfaceType);
		}
		else {
			attributesType.getDeclaredMembers().add(field);
			field.setParent(attributesType);
		}
	}


	private static boolean isEmpty(TSInterfaceType type) {
		return type.getDeclaredMembers().isEmpty() && type.getImplementedInterfaces().isEmpty();
	}

	private static void generateAttributes(TSMetaTransformationContext context, TSInterfaceType interfaceType,
			MetaDataObject element) {
		for (MetaAttribute attr : element.getDeclaredAttributes()) {
			MetaType elementType = attr.getType().getElementType();

			TSField field = new TSField();
			field.setName(attr.getName());
			field.setType((TSType) context.transform(elementType, TSMetaTransformationOptions.EMPTY));
			field.setNullable(true);
			interfaceType.getDeclaredMembers().add(field);
		}
	}

	private static void setupParent(TSMetaTransformationContext context, TSInterfaceType interfaceType,
			MetaDataObject metaDataObject) {
		TSContainerElement parent = null;

		// move links and meta information to the resource itself
		boolean isMeta = TypescriptUtils.isInstance(metaDataObject.getImplementationClass(),
				"io.crnk.core.resource.meta.MetaInformation");
		boolean isLinks = TypescriptUtils.isInstance(metaDataObject.getImplementationClass(),
				"io.crnk.core.resource.links.LinksInformation");
		if ((isMeta || isLinks) && metaDataObject.getImplementationClass().getEnclosingClass() != null) {
			MetaElement enclosingMeta = context.getMeta(metaDataObject.getImplementationClass().getEnclosingClass());
			if (enclosingMeta instanceof MetaResource) {
				TSType enclosingType = (TSType) context.transform(enclosingMeta, TSMetaTransformationOptions.EMPTY);
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

	@Override
	public boolean isRoot(MetaElement element) {
		return false;
	}

}
