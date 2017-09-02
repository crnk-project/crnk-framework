package io.crnk.core.engine.information.resource;

import io.crnk.core.utils.Optional;

/**
 * Provides information necessary to properly serializer (or skip) resource attributes.
 * 
 * @author Craig Setera
 */
public interface ResourceFieldInformationProvider {
	/**
	 * Return a boolean indicating whether the specified field should be ignored when doing serialization.
	 * 
	 * @param resourceClass
	 * @param resourceField
	 * @return
	 */
	Optional<Boolean> isIgnored(Class<?> resourceClass, ResourceField resourceField);
}
