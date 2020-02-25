package io.crnk.gen.typescript.transform;


import io.crnk.gen.typescript.internal.TypescriptUtils;
import io.crnk.gen.typescript.model.TSArrayType;
import io.crnk.gen.typescript.model.TSContainerElement;
import io.crnk.gen.typescript.model.TSElement;
import io.crnk.gen.typescript.model.TSField;
import io.crnk.gen.typescript.model.TSInterfaceType;
import io.crnk.gen.typescript.model.TSMember;
import io.crnk.gen.typescript.model.TSType;
import io.crnk.gen.typescript.model.libraries.NgrxJsonApiLibrary;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceRepository;

/**
 * Transforms MetaResourceRepository elements to (One/Many)QueryResult interfaces to gain type-safe access
 * to JSON API repositories.
 */
public class TSMetaResourceRepositoryTransformation implements TSMetaTransformation {

	@Override
	public boolean accepts(MetaElement element) {
		return element instanceof MetaResourceRepository;
	}

	@Override
	public TSElement transform(MetaElement element, TSMetaTransformationContext context, TSMetaTransformationOptions options) {
		MetaResourceRepository metaRepository = (MetaResourceRepository) element;

		MetaResource metaResource = metaRepository.getResourceType();
		TSType resourceType = context.transform(metaResource, TSMetaTransformationOptions.EMPTY).asType();
		TSContainerElement parent = (TSContainerElement) resourceType.getParent();

		MetaDataObject metaListLinks = metaRepository.getListLinksType();
		MetaDataObject metaListMeta = metaRepository.getListMetaType();

		TSInterfaceType oneResultType = new TSInterfaceType();
		oneResultType.setName(TypescriptUtils.toClassName(metaResource) + "Result");
		oneResultType.setExported(true);
		oneResultType.addImplementedInterface(NgrxJsonApiLibrary.ONE_QUERY_RESULT);
		oneResultType.addDeclaredMember(newDataField(context, resourceType, false));
		parent.addElement(oneResultType);

		TSInterfaceType manyResultType = new TSInterfaceType();
		manyResultType.setName(TypescriptUtils.toClassName(metaResource) + "ListResult");
		manyResultType.setExported(true);
		manyResultType.addImplementedInterface(NgrxJsonApiLibrary.MANY_QUERY_RESULT);
		manyResultType.addDeclaredMember(newDataField(context, resourceType, true));
		parent.addElement(manyResultType);

		if (metaListLinks != null) {
			TSMetaTransformationOptions listOptions = new TSMetaTransformationOptions();
			listOptions.setParent(TypescriptUtils.getNestedTypeContainer(manyResultType, true));

			TSType linksType = context.transform(metaListLinks, listOptions).asType();
			TSField field = new TSField();
			field.setName("links");
			field.setNullable(true);
			field.setType(linksType);
			manyResultType.addDeclaredMember(field);
		}

		if (metaListMeta != null) {
			TSMetaTransformationOptions listOptions = new TSMetaTransformationOptions();
			listOptions.setParent(TypescriptUtils.getNestedTypeContainer(manyResultType, true));

			TSType metaType = context.transform(metaListMeta, listOptions).asType();
			TSField field = new TSField();
			field.setName("meta");
			field.setNullable(true);
			field.setType(metaType);
			manyResultType.addDeclaredMember(field);
		}

		return null;
	}

	@Override
	public void postTransform(TSElement element, TSMetaTransformationContext context) {

	}

	private TSMember newDataField(TSMetaTransformationContext context, TSType resourceType, boolean isArray) {
		TSField field = new TSField();
		field.setName("data");
		field.setNullable(true);
		if (isArray) {
			field.setType(new TSArrayType(resourceType));
		} else {
			field.setType(resourceType);
		}
		return field;
	}

	@Override
	public boolean isRoot(MetaElement element) {
		return element instanceof MetaResourceRepository;
	}
}