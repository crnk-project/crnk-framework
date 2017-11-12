package io.crnk.core.engine.internal.information.resource;

import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.information.bean.BeanInformation;
import io.crnk.core.engine.information.resource.*;
import io.crnk.core.engine.internal.document.mapper.IncludeLookupUtil;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.SerializeType;
import io.crnk.core.utils.Optional;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;


public abstract class ResourceInformationProviderBase implements ResourceInformationProvider {

	protected ResourceInformationProviderContext context;

	protected List<ResourceFieldInformationProvider> resourceFieldInformationProviders;

	private LookupIncludeBehavior globalLookupIncludeBehavior;

	public ResourceInformationProviderBase(
		PropertiesProvider propertiesProvider,
		List<ResourceFieldInformationProvider> resourceFieldInformationProviders)
	{
		this.resourceFieldInformationProviders = resourceFieldInformationProviders;
		this.globalLookupIncludeBehavior = IncludeLookupUtil.getGlolbalLookupIncludeBehavior(propertiesProvider);
	}

	@Override
	public void init(ResourceInformationProviderContext context) {
		this.context = context;

		for (ResourceFieldInformationProvider resourceFieldInformationProvider : resourceFieldInformationProviders) {
			resourceFieldInformationProvider.init(context);
		}
	}

	protected List<ResourceField> getResourceFields(Class<?> resourceClass) {
		BeanInformation beanDesc = new BeanInformation(resourceClass);
		List<String> attributeNames = beanDesc.getAttributeNames();
		List<ResourceField> fields = new ArrayList<>();
		for (String attributeName : attributeNames) {
			BeanAttributeInformation attributeDesc = beanDesc.getAttribute(attributeName);
			if (!isIgnored(attributeDesc)) {
				InformationBuilder informationBuilder = context.getInformationBuilder();
				InformationBuilder.Field fieldBuilder = informationBuilder.createResourceField();
				buildResourceField(attributeDesc, fieldBuilder);
				fields.add(fieldBuilder.build());
			}
		}
		return fields;
	}

	protected void buildResourceField(BeanAttributeInformation attributeDesc, InformationBuilder.Field fieldBuilder) {
		fieldBuilder.underlyingName(attributeDesc.getName());
		fieldBuilder.jsonName(getJsonName(attributeDesc));

		ResourceFieldType fieldType = getFieldType(attributeDesc);
		fieldBuilder.fieldType(fieldType);
		fieldBuilder.access(getAccess(attributeDesc, fieldType));
		fieldBuilder.serializeType(getSerializeType(attributeDesc, fieldType));
		fieldBuilder.lookupIncludeBehavior(getLookupIncludeBehavior(attributeDesc));

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
			fieldBuilder.oppositeResourceType(getResourceType(genericType, context));
			fieldBuilder.oppositeName(getOppositeName(attributeDesc));
		}
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

	protected LookupIncludeBehavior getLookupIncludeBehavior(BeanAttributeInformation attributeDesc) {
		LookupIncludeBehavior behavior = LookupIncludeBehavior.DEFAULT;

		for (ResourceFieldInformationProvider fieldInformationProvider : resourceFieldInformationProviders) {
			Optional<LookupIncludeBehavior> lookupIncludeBehavior = fieldInformationProvider.getLookupIncludeBehavior(attributeDesc);
			if (lookupIncludeBehavior.isPresent()) {
				behavior = lookupIncludeBehavior.get();
				break;
			}
		}

		// If the field-level behavior is DEFAULT, then look to the global setting
		if (behavior == LookupIncludeBehavior.DEFAULT) {
			behavior = globalLookupIncludeBehavior;
		}

		// If the global behavior was also default, fall all they way back to the
		// information provider's default
		if (behavior == LookupIncludeBehavior.DEFAULT) {
			behavior = getDefaultLookupIncludeBehavior();
		}

		return behavior;
	}

	protected LookupIncludeBehavior getDefaultLookupIncludeBehavior() {
		return LookupIncludeBehavior.NONE;
	}

	private ResourceFieldAccess getAccess(BeanAttributeInformation attributeDesc, ResourceFieldType resourceFieldType) {
		boolean sortable = isSortable(attributeDesc);
		boolean filterable = isFilterable(attributeDesc);
		boolean postable = isPostable(attributeDesc);
		boolean patchable = isPatchable(attributeDesc, resourceFieldType);
		boolean readable = isReadable(attributeDesc);
		return new ResourceFieldAccess(readable, postable, patchable, sortable, filterable);
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


	private ResourceFieldType getFieldType(BeanAttributeInformation attributeDesc) {
		for (ResourceFieldInformationProvider fieldInformationProvider : resourceFieldInformationProviders) {
			Optional<ResourceFieldType> fieldType = fieldInformationProvider.getFieldType(attributeDesc);
			if (fieldType.isPresent()) {
				return fieldType.get();
			}
		}
		return ResourceFieldType.ATTRIBUTE;
	}

	private String getJsonName(BeanAttributeInformation attributeDesc) {
		for (ResourceFieldInformationProvider fieldInformationProvider : resourceFieldInformationProviders) {
			Optional<String> jsonName = fieldInformationProvider.getJsonName(attributeDesc);
			if (jsonName.isPresent()) {
				return jsonName.get();
			}
		}
		return attributeDesc.getName();
	}

	private String getOppositeName(BeanAttributeInformation attributeDesc) {
		for (ResourceFieldInformationProvider fieldInformationProvider : resourceFieldInformationProviders) {
			Optional<String> oppositeName = fieldInformationProvider.getOppositeName(attributeDesc);
			if (oppositeName.isPresent()) {
				return oppositeName.get();
			}
		}
		return null;
	}
}
