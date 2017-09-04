package io.crnk.core.engine.information.resource;

import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.SerializeType;

import java.lang.reflect.Type;

public interface ResourceField {

	ResourceFieldType getResourceFieldType();

	/**
	 * See also
	 * {@link io.crnk.core.resource.annotations.JsonApiLookupIncludeAutomatically}
	 * }
	 *
	 * @return if lookup should be performed
	 */
	LookupIncludeBehavior getLookupIncludeAutomatically();

	/**
	 * @return name of opposite attribute in case of a bidirectional relation.
	 */
	String getOppositeName();

	/**
	 * @return resourceType of the opposite resource in case of a relation.
	 */
	String getOppositeResourceType();

	/**
	 * @return name used in Json
	 */
	String getJsonName();

	/**
	 * @return name used in Java
	 */
	String getUnderlyingName();

	Class<?> getType();

	Type getGenericType();

	SerializeType getSerializeType();

	/**
	 * @return the non-collection type. Matches {@link #getType()} for
	 * non-collections. Returns the type argument in case of a
	 * collection type.
	 */
	Class<?> getElementType();

	/**
	 * @return resourceInformation this field belongs to.
	 */
	ResourceInformation getParentResourceInformation();

	void setResourceInformation(ResourceInformation resourceInformation);

	boolean isCollection();

	ResourceFieldAccessor getAccessor();

	/**
	 * @return access information for this resource (postable, patchable)
	 */
	ResourceFieldAccess getAccess();
}