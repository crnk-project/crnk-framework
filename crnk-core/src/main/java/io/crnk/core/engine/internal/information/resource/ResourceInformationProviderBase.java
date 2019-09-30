package io.crnk.core.engine.internal.information.resource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.information.bean.BeanInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceFieldInformationProvider;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.information.resource.ResourceInformationProviderContext;
import io.crnk.core.engine.information.resource.VersionRange;
import io.crnk.core.engine.internal.document.mapper.IncludeLookupUtil;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.FieldOrderedComparator;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.exception.InvalidResourceException;
import io.crnk.core.resource.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class ResourceInformationProviderBase implements ResourceInformationProvider {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	protected ResourceInformationProviderContext context;

	protected List<ResourceFieldInformationProvider> resourceFieldInformationProviders;

	private LookupIncludeBehavior globalLookupIncludeBehavior;

	private boolean enforceIdName;

	public ResourceInformationProviderBase(
			PropertiesProvider propertiesProvider,
			List<ResourceFieldInformationProvider> resourceFieldInformationProviders) {
		this.resourceFieldInformationProviders = resourceFieldInformationProviders;
		this.globalLookupIncludeBehavior = IncludeLookupUtil.getGlobalLookupIncludeBehavior(propertiesProvider);

		String strEnforceIdName = propertiesProvider.getProperty(CrnkProperties.ENFORCE_ID_NAME);
		this.enforceIdName = strEnforceIdName == null || Boolean.parseBoolean(strEnforceIdName);
	}

	protected VersionRange getVersionRange(Class<?> resourceClass) {
		JsonApiVersion annotation = resourceClass.getAnnotation(JsonApiVersion.class);
		if (annotation != null) {
			return VersionRange.of(annotation.min(), annotation.max());
		}
		return VersionRange.UNBOUNDED;
	}

	@Override
	public void init(ResourceInformationProviderContext context) {
		this.context = context;

		for (ResourceFieldInformationProvider resourceFieldInformationProvider : resourceFieldInformationProviders) {
			resourceFieldInformationProvider.init(context);
		}
	}

	protected ResourceFieldAccess getResourceAccess(Class<?> resourceClass) {
		boolean sortable = true;
		boolean filterable = true;
		boolean postable = true;
		boolean deletable = true;
		boolean patchable = true;
		boolean readable = true;
		JsonApiResource annotation = resourceClass.getAnnotation(JsonApiResource.class);
		if (annotation != null) {
			sortable = annotation.sortable();
			filterable = annotation.filterable();
			postable = annotation.postable();
			deletable = annotation.deletable();
			patchable = annotation.patchable();
			readable = annotation.readable();
		}
		return new ResourceFieldAccess(readable, postable, patchable, deletable, sortable, filterable);
	}

	protected List<ResourceField> getResourceFields(Class<?> resourceClass, ResourceFieldAccess resourceAccess, boolean embedded) {
		BeanInformation beanDesc = BeanInformation.get(resourceClass);
		List<String> attributeNames = beanDesc.getAttributeNames();
		List<ResourceField> fields = new ArrayList<>();
		Set<String> relationIdFields = new HashSet<>();
		for (String attributeName : attributeNames) {
			BeanAttributeInformation attributeDesc = beanDesc.getAttribute(attributeName);
			if (!isIgnored(attributeDesc)) {
				InformationBuilder informationBuilder = context.getInformationBuilder();
				InformationBuilder.FieldInformationBuilder fieldBuilder = informationBuilder.createResourceField();
				buildResourceField(beanDesc, embedded, attributeDesc, fieldBuilder);
				fields.add(fieldBuilder.build());
			} else if (attributeDesc.getAnnotation(JsonApiRelationId.class).isPresent()) {
				relationIdFields.add(attributeDesc.getName());
			}
		}

		if (!embedded) {
			verifyRelationIdFields(resourceClass, relationIdFields, fields);
		}

		for (ResourceField resourceField : fields) {
			ResourceFieldImpl impl = (ResourceFieldImpl) resourceField;
			impl.setAccess(impl.getAccess().and(resourceAccess));
		}

		Optional<JsonPropertyOrder> propertyOrder = ClassUtils.getAnnotation(resourceClass, JsonPropertyOrder.class);
		if (propertyOrder.isPresent()) {
			JsonPropertyOrder propertyOrderAnnotation = propertyOrder.get();
			Collections.sort(fields,
					new FieldOrderedComparator(propertyOrderAnnotation.value(), propertyOrderAnnotation.alphabetic()));
		}

		return fields;
	}

	private void verifyRelationIdFields(Class resourceClass, Set<String> relationIdFields, List<ResourceField> fields) {
		for (ResourceField field : fields) {
			if (field.getResourceFieldType() == ResourceFieldType.RELATIONSHIP && field.hasIdField()) {
				relationIdFields.remove(field.getIdName());
			}
		}

		if (!relationIdFields.isEmpty()) {
			throw new InvalidResourceException(resourceClass.getName() + " annotated " + relationIdFields + " with "
					+ "@JsonApiRelationId but no matching relationship found");
		}
	}

	protected void buildResourceField(BeanInformation beanDesc, boolean embedded, BeanAttributeInformation attributeDesc, InformationBuilder.FieldInformationBuilder
			fieldBuilder) {
		fieldBuilder.underlyingName(attributeDesc.getName());
		ResourceFieldType fieldType = getFieldType(attributeDesc, embedded);
		fieldBuilder.jsonName(getJsonName(attributeDesc, fieldType));

		ResourceFieldAccess access = getAccess(attributeDesc, fieldType);
		fieldBuilder.fieldType(fieldType);
		fieldBuilder.access(access);
		fieldBuilder.patchStrategy(getPatchStrategy(attributeDesc));
		fieldBuilder.jsonIncludeStrategy(getJsonIncludeStrategy(attributeDesc));
		fieldBuilder.serializeType(getSerializeType(attributeDesc, fieldType));
		fieldBuilder.relationshipRepositoryBehavior(getRelationshipRepositoryBehavior(attributeDesc));
		fieldBuilder.versionRange(getVersionRange(attributeDesc));

		Type genericType;
		if (useFieldType(attributeDesc)) {
			fieldBuilder.type(attributeDesc.getField().getType());
			genericType = attributeDesc.getField().getGenericType();
		} else {
			fieldBuilder.type(attributeDesc.getGetter().getReturnType());
			genericType = attributeDesc.getGetter().getGenericReturnType();
		}
		fieldBuilder.genericType(genericType);
		if (fieldType == ResourceFieldType.RELATIONSHIP) {

			Optional<String> mappedBy = getMappedBy(attributeDesc);
			if (mappedBy.isPresent() && !mappedBy.get().isEmpty()) {
				fieldBuilder.setMappedBy(true);
			}

			fieldBuilder.oppositeResourceType(getResourceType(attributeDesc.getType(), context));
			fieldBuilder.oppositeName(getOppositeName(attributeDesc));

			Optional<JsonApiRelation> relationAnnotation = attributeDesc.getAnnotation(JsonApiRelation.class);
			boolean multiValued = Collection.class.isAssignableFrom(attributeDesc.getImplementationClass());
			String suffix = multiValued ? "Ids" : "Id";
			String idFieldName;
			boolean hasIdNameReference = relationAnnotation.isPresent() && relationAnnotation.get().idField().length() > 0;
			if (hasIdNameReference) {
				idFieldName = relationAnnotation.get().idField();
			} else {
				idFieldName = attributeDesc.getName() + suffix;
			}
			BeanAttributeInformation idAttribute = beanDesc.getAttribute(idFieldName);
			if (idAttribute == null && multiValued && attributeDesc.getName().endsWith("s")) {
				// also try to correlate by removing ending s
				String idFieldNameTemp = attributeDesc.getName().substring(0, attributeDesc.getName().length() - 1) + suffix;
				BeanAttributeInformation idAttributeTemp = beanDesc.getAttribute(idFieldNameTemp);
				// make sure there are no custom get-named methods
				if (idAttributeTemp != null && attributeDesc.getGetter() != null && idAttributeTemp.getGetter().getReturnType().equals(attributeDesc.getGetter().getReturnType())) {
					idFieldName = idFieldNameTemp;
					idAttribute = idAttributeTemp;
				}
			}

			// if id field has been explicitly declared, then it must also exist
			PreconditionUtil.verify(idAttribute != null || !hasIdNameReference, "idField %s not found for %s", idFieldName, attributeDesc);

			if (idAttribute != null && (hasIdNameReference || idAttribute.getAnnotation(JsonApiRelationId.class).isPresent())) {
				fieldBuilder.idName(idFieldName);
				fieldBuilder.idType(idAttribute.getImplementationClass());
			}

			fieldBuilder.lookupIncludeBehavior(getLookupIncludeBehavior(attributeDesc, idAttribute != null));
		}

		if (isEmbeddedType(attributeDesc)) {
			Class elementType = ClassUtils.getRawType(ClassUtils.getElementType(attributeDesc.getImplementationType()));
			InformationBuilder.EmbeddableInformationBuilder embBuilder = fieldBuilder.embeddedType(elementType);
			List<ResourceField> embFields = getResourceFields(elementType, access, true);
			embFields.forEach(field -> embBuilder.addField().from(field));
		}
	}

	protected VersionRange getVersionRange(BeanAttributeInformation attributeDesc) {
		for (ResourceFieldInformationProvider fieldInformationProvider : resourceFieldInformationProviders) {
			Optional<VersionRange> versionRange = fieldInformationProvider.getVersionRange(attributeDesc);
			if (versionRange.isPresent()) {
				return versionRange.get();
			}
		}
		return VersionRange.UNBOUNDED;
	}

	protected RelationshipRepositoryBehavior getRelationshipRepositoryBehavior(BeanAttributeInformation attributeDesc) {
		for (ResourceFieldInformationProvider fieldInformationProvider : resourceFieldInformationProviders) {
			Optional<RelationshipRepositoryBehavior> behavior =
					fieldInformationProvider.getRelationshipRepositoryBehavior(attributeDesc);
			if (behavior.isPresent()) {
				return behavior.get();
			}
		}
		return getDefaultRelationshipRepositoryBehavior(attributeDesc);
	}

	protected Optional<String> getMappedBy(BeanAttributeInformation attributeDesc) {
		Optional<String> mappedBy = Optional.empty();
		for (ResourceFieldInformationProvider fieldInformationProvider : resourceFieldInformationProviders) {
			Optional<String> opt = fieldInformationProvider.getMappedBy(attributeDesc);
			if (opt.isPresent() && (!mappedBy.isPresent() || mappedBy.get().isEmpty())) {
				mappedBy = opt;
			}
		}
		return mappedBy;
	}

	protected RelationshipRepositoryBehavior getDefaultRelationshipRepositoryBehavior(BeanAttributeInformation attributeDesc) {
		return RelationshipRepositoryBehavior.DEFAULT;
	}

	private static String getResourceType(Type genericType, ResourceInformationProviderContext context) {
		Type elementType = genericType;
		if (Iterable.class.isAssignableFrom(ClassUtils.getRawType(genericType))) {
			elementType = ClassUtils.getRawType(((ParameterizedType) genericType).getActualTypeArguments()[0]);
		}
		Class<?> rawType = ClassUtils.getRawType(elementType);
		return context.accept(rawType) ? context.getResourceType(rawType) : null;
	}


	private boolean useFieldType(BeanAttributeInformation attributeDesc) {
		for (ResourceFieldInformationProvider fieldInformationProvider : resourceFieldInformationProviders) {
			Optional<Boolean> jsonName = fieldInformationProvider.useFieldType(attributeDesc);
			if (jsonName.isPresent()) {
				return jsonName.get();
			}
		}
		return attributeDesc.getGetter() == null;
	}

	private SerializeType getSerializeType(BeanAttributeInformation attributeDesc, ResourceFieldType resourceFieldType) {
		for (ResourceFieldInformationProvider fieldInformationProvider : resourceFieldInformationProviders) {
			Optional<SerializeType> lazy = fieldInformationProvider.getSerializeType(attributeDesc);
			if (lazy.isPresent()) {
				return lazy.get();
			}
		}
		return resourceFieldType == ResourceFieldType.RELATIONSHIP ? SerializeType.LAZY : SerializeType.EAGER;
	}

	public JsonIncludeStrategy getJsonIncludeStrategy(BeanAttributeInformation attributeDesc) {
		for (ResourceFieldInformationProvider fieldInformationProvider : resourceFieldInformationProviders) {
			Optional<JsonIncludeStrategy> jsonIncludeStrategy = fieldInformationProvider.getJsonIncludeStrategy(attributeDesc);
			if (jsonIncludeStrategy.isPresent()) {
				return jsonIncludeStrategy.get();
			}
		}
		if (attributeDesc.getImplementationClass() == Optional.class) {
			return JsonIncludeStrategy.NOT_NULL; // Optional.empty() is serialized as null
		}
		return JsonIncludeStrategy.DEFAULT;
	}

	protected boolean isEmbeddedType(BeanAttributeInformation attributeDesc) {
		for (ResourceFieldInformationProvider fieldInformationProvider : resourceFieldInformationProviders) {
			if (fieldInformationProvider.isEmbeddedType(attributeDesc)) {
				return true;
			}
		}
		return false;
	}

	protected LookupIncludeBehavior getLookupIncludeBehavior(BeanAttributeInformation attributeDesc, boolean hasIdField) {
		LookupIncludeBehavior behavior = LookupIncludeBehavior.DEFAULT;

		for (ResourceFieldInformationProvider fieldInformationProvider : resourceFieldInformationProviders) {
			Optional<LookupIncludeBehavior> lookupIncludeBehavior =
					fieldInformationProvider.getLookupIncludeBehavior(attributeDesc);
			if (lookupIncludeBehavior.isPresent()) {
				behavior = lookupIncludeBehavior.get();
				break;
			}
		}

		if (behavior != LookupIncludeBehavior.DEFAULT) {
			LOGGER.debug("{}: using configured LookupIncludeBehavior.{}", attributeDesc, behavior);
			return behavior;
		}

		// If the global behavior was also default, fall all they way back to the
		// information provider's default
		return getDefaultLookupIncludeBehavior(attributeDesc);
	}

	protected LookupIncludeBehavior getDefaultLookupIncludeBehavior(BeanAttributeInformation attributeDesc) {
		// If the field-level behavior is DEFAULT, then look to the global setting
		if (globalLookupIncludeBehavior != LookupIncludeBehavior.DEFAULT) {
			LOGGER.debug("{}: using global/configured default LookupIncludeBehavior.{}", attributeDesc, globalLookupIncludeBehavior);
			return globalLookupIncludeBehavior;
		}

		return LookupIncludeBehavior.DEFAULT;
	}

	private ResourceFieldAccess getAccess(BeanAttributeInformation attributeDesc, ResourceFieldType resourceFieldType) {
		boolean sortable = isSortable(attributeDesc);
		boolean filterable = isFilterable(attributeDesc);
		boolean postable = isPostable(attributeDesc);
		boolean deletable = isDeletable(attributeDesc);
		boolean patchable = isPatchable(attributeDesc, resourceFieldType);
		boolean readable = isReadable(attributeDesc);
		return new ResourceFieldAccess(readable, postable, patchable, deletable, sortable, filterable);
	}

	private boolean isSortable(BeanAttributeInformation attributeDesc) {
		for (ResourceFieldInformationProvider fieldInformationProvider : resourceFieldInformationProviders) {
			Optional<Boolean> sortable = fieldInformationProvider.isSortable(attributeDesc);
			if (sortable.isPresent()) {
				return sortable.get();
			}
		}
		return true;
	}

	private boolean isFilterable(BeanAttributeInformation attributeDesc) {
		for (ResourceFieldInformationProvider fieldInformationProvider : resourceFieldInformationProviders) {
			Optional<Boolean> filterable = fieldInformationProvider.isFilterable(attributeDesc);
			if (filterable.isPresent()) {
				return filterable.get();
			}
		}
		return true;
	}

	private boolean isPatchable(BeanAttributeInformation attributeDesc, ResourceFieldType resourceFieldType) {
		if (isReadOnly(attributeDesc) || resourceFieldType == ResourceFieldType.ID) {
			return false;
		}
		for (ResourceFieldInformationProvider fieldInformationProvider : resourceFieldInformationProviders) {
			Optional<Boolean> patchable = fieldInformationProvider.isPatchable(attributeDesc);
			if (patchable.isPresent()) {
				return patchable.get();
			}
		}
		return true;
	}

	private boolean isPostable(BeanAttributeInformation attributeDesc) {
		if (isReadOnly(attributeDesc)) {
			return false;
		}
		for (ResourceFieldInformationProvider fieldInformationProvider : resourceFieldInformationProviders) {
			Optional<Boolean> postable = fieldInformationProvider.isPostable(attributeDesc);
			if (postable.isPresent()) {
				return postable.get();
			}
		}
		return true;
	}

	private boolean isDeletable(BeanAttributeInformation attributeDesc) {
		if (isReadOnly(attributeDesc)) {
			return false;
		}
		for (ResourceFieldInformationProvider fieldInformationProvider : resourceFieldInformationProviders) {
			Optional<Boolean> deletable = fieldInformationProvider.isDeletable(attributeDesc);
			if (deletable.isPresent()) {
				return deletable.get();
			}
		}
		return true;
	}


	private boolean isReadable(BeanAttributeInformation attributeDesc) {
		for (ResourceFieldInformationProvider fieldInformationProvider : resourceFieldInformationProviders) {
			Optional<Boolean> readable = fieldInformationProvider.isReadable(attributeDesc);
			if (readable.isPresent()) {
				return readable.get();
			}
		}
		return true;
	}

	private boolean isReadOnly(BeanAttributeInformation attributeDesc) {
		Field field = attributeDesc.getField();
		Method setter = attributeDesc.getSetter();
		return setter == null && (field == null || !Modifier.isPublic(field.getModifiers()));
	}

	private boolean isIgnored(BeanAttributeInformation attributeDesc) {
		for (ResourceFieldInformationProvider fieldInformationProvider : resourceFieldInformationProviders) {
			Optional<Boolean> ignored = fieldInformationProvider.isIgnored(attributeDesc);
			if (ignored.isPresent()) {
				return ignored.get();
			}
		}
		return false;
	}

	private PatchStrategy getPatchStrategy(BeanAttributeInformation attributeDesc) {
		PatchStrategy strategy = PatchStrategy.DEFAULT;
		for (ResourceFieldInformationProvider fieldInformationProvider : resourceFieldInformationProviders) {
			Optional<PatchStrategy> patchStrategy = fieldInformationProvider.getPatchStrategy(attributeDesc);
			if (patchStrategy.isPresent()) {
				strategy = patchStrategy.get();
				break;
			}
		}
		if (strategy == PatchStrategy.DEFAULT) {
			strategy = PatchStrategy.MERGE;
		}

		return strategy;
	}

	private ResourceFieldType getFieldType(BeanAttributeInformation attributeDesc, boolean embedded) {
		for (ResourceFieldInformationProvider fieldInformationProvider : resourceFieldInformationProviders) {
			Optional<ResourceFieldType> fieldType = fieldInformationProvider.getFieldType(attributeDesc);
			if (fieldType.isPresent()) {
				ResourceFieldType type = fieldType.get();
				if (embedded && ResourceFieldType.RELATIONSHIP == type) {
					// nested relationships not supported and treated as regular attributes
					return ResourceFieldType.ATTRIBUTE;
				}
				return type;
			}
		}
		return ResourceFieldType.ATTRIBUTE;
	}

	protected String getJsonName(BeanAttributeInformation attributeDesc, ResourceFieldType fieldType) {
		if (fieldType == ResourceFieldType.ID && enforceIdName) {
			// referred to as id even if named differently in Java
			return "id";
		}
		for (ResourceFieldInformationProvider fieldInformationProvider : resourceFieldInformationProviders) {
			Optional<String> jsonName = fieldInformationProvider.getJsonName(attributeDesc);
			if (jsonName.isPresent()) {
				return jsonName.get();
			}
		}
		return attributeDesc.getName();
	}

	private String getOppositeName(BeanAttributeInformation attributeDesc) {
		Optional<String> mappedBy = getMappedBy(attributeDesc);
		if (mappedBy.isPresent() && !mappedBy.get().isEmpty()) {
			return mappedBy.get();
		}

		for (ResourceFieldInformationProvider fieldInformationProvider : resourceFieldInformationProviders) {
			Optional<String> oppositeName = fieldInformationProvider.getOppositeName(attributeDesc);
			if (oppositeName.isPresent()) {
				return oppositeName.get();
			}
		}
		return null;
	}
}
