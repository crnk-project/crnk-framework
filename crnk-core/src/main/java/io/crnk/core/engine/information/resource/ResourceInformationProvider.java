package io.crnk.core.engine.information.resource;

/**
 * A builder which creates ResourceInformation instances of a specific class.
 */
public interface ResourceInformationProvider {

	/**
	 * @param resourceClass resource class
	 * @return true if this builder can process the provided resource class
	 */
	boolean accept(Class<?> resourceClass);

	/**
	 * @param resourceClass resource class
	 * @return ResourceInformation for the provided resource class.
	 */
	ResourceInformation build(Class<?> resourceClass);

	void init(ResourceInformationProviderContext context);

	String getResourceType(Class<?> clazz);

}
