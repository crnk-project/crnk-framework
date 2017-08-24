package io.crnk.core.engine.information.resource;

/**
 * Provides information necessary to properly serializer (or skip) resource attributes.
 * 
 * @author Craig Setera
 */
public interface AttributeSerializationInformationProvider {
	/**
	 * Return a boolean indicating whether the specified field should be ignored when doing serialization.
	 * 
	 * @param resourceClass
	 * @param resourceField
	 * @return
	 */
	boolean isIgnored(Class<?> resourceClass, ResourceField resourceField);
}
