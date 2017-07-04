package io.crnk.core.engine.internal.information.resource;

import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.information.resource.*;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

public class ResourceFieldImpl implements ResourceField {

	private final String jsonName;

	private final String underlyingName;

	private final Class<?> type;

	private final Type genericType;

	private final boolean lazy;

	private final String oppositeResourceType;

	private final LookupIncludeBehavior lookupIncludeBehavior;

	private final boolean includeByDefault;

	private final ResourceFieldType resourceFieldType;

	private final String oppositeName;

	private ResourceInformation parentResourceInformation;

	private ResourceFieldAccessor accessor;

	private final ResourceFieldAccess access;

	public ResourceFieldImpl(String jsonName, String underlyingName, ResourceFieldType resourceFieldType, Class<?> type,
							 Type genericType, String oppositeResourceType) {
		this(jsonName, underlyingName, resourceFieldType, type, genericType,
				oppositeResourceType, null, true, false, LookupIncludeBehavior.NONE,
				new ResourceFieldAccess(true, true, true, true));
	}

	public ResourceFieldImpl(String jsonName, String underlyingName, ResourceFieldType resourceFieldType, Class<?> type,
							 Type genericType, String oppositeResourceType, String oppositeName, boolean lazy,
							 boolean includeByDefault, LookupIncludeBehavior lookupIncludeBehavior,
							 ResourceFieldAccess access) {
		this.jsonName = jsonName;
		this.underlyingName = underlyingName;
		this.resourceFieldType = resourceFieldType;
		this.includeByDefault = includeByDefault;
		this.type = type;
		this.genericType = genericType;
		this.lazy = lazy;
		this.lookupIncludeBehavior = lookupIncludeBehavior;
		this.oppositeName = oppositeName;
		this.oppositeResourceType = oppositeResourceType;
		this.access = access;
	}

	public ResourceFieldType getResourceFieldType() {
		return resourceFieldType;
	}

	/**
	 * See also
	 * {@link io.crnk.core.resource.annotations.JsonApiLookupIncludeAutomatically}
	 * }
	 *
	 * @return if lookup should be performed
	 */
	public LookupIncludeBehavior getLookupIncludeAutomatically() {
		return lookupIncludeBehavior;
	}

	/**
	 * @return name of opposite attribute in case of a bidirectional relation.
	 */
	public String getOppositeName() {
		return oppositeName;
	}

	public String getJsonName() {
		return jsonName;
	}

	public String getUnderlyingName() {
		return underlyingName;
	}

	public String getOppositeResourceType() {
		PreconditionUtil.assertEquals("not an association", ResourceFieldType.RELATIONSHIP, resourceFieldType);
		if (getElementType() != Object.class) {
			PreconditionUtil.assertNotNull("resourceType must not be null", oppositeResourceType);
		}
		return oppositeResourceType;
	}

	public Class<?> getType() {
		return type;
	}

	public Type getGenericType() {
		return genericType;
	}

	/**
	 * Returns a flag which indicate if a field should not be serialized
	 * automatically.
	 *
	 * @return true if a field is lazy
	 */
	public boolean isLazy() {
		return lazy;
	}

	public boolean getIncludeByDefault() {
		return includeByDefault;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ResourceFieldImpl that = (ResourceFieldImpl) o;
		return Objects.equals(jsonName, that.jsonName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(jsonName);
	}

	/**
	 * Returns the non-collection type. Matches {@link #getType()} for
	 * non-collections. Returns the type argument in case of a collection type.
	 *
	 * @return Ask Remmo
	 */
	public Class<?> getElementType() {
		if (Iterable.class.isAssignableFrom(type) && getGenericType() instanceof Class) {
			return Object.class;
		}
		if (Iterable.class.isAssignableFrom(type)) {
			return (Class<?>) ((ParameterizedType) getGenericType()).getActualTypeArguments()[0];
		}
		return type;
	}

	public ResourceInformation getParentResourceInformation() {
		return parentResourceInformation;
	}

	@Override
	public ResourceFieldAccessor getAccessor() {
		PreconditionUtil.assertNotNull("field not properly initialized", accessor);
		return accessor;
	}

	public void setAccessor(ResourceFieldAccessor accessor) {
		// TODO to be eliminated by a builder pattern soon
		this.accessor = accessor;
	}

	public void setResourceInformation(ResourceInformation resourceInformation) {
		if (this.accessor == null && resourceInformation.getResourceClass() == Resource.class) {
			this.accessor = new RawResourceFieldAccessor(underlyingName, resourceFieldType, type);
		} else if (this.accessor == null) {
			this.accessor = new ReflectionFieldAccessor(resourceInformation.getResourceClass(), underlyingName, type);
		}
		this.parentResourceInformation = resourceInformation;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[jsonName=").append(jsonName);
		if (parentResourceInformation != null && parentResourceInformation.getResourceType() != null) {
			sb.append(",resourceType=").append(parentResourceInformation);
		}
		sb.append("]");
		return sb.toString();
	}

	public boolean isCollection() {
		return Iterable.class.isAssignableFrom(getType());
	}

	@Override
	public ResourceFieldAccess getAccess() {
		return access;
	}
}