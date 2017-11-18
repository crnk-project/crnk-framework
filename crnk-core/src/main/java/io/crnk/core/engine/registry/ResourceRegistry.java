package io.crnk.core.engine.registry;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.url.ServiceUrlProvider;

/**
 * ${@link ResourceRegistryPart} implementation if a number of convenience methods used and exposed
 * trough the Module API.
 */
public interface ResourceRegistry extends ResourceRegistryPart {

	RegistryEntry findEntry(Class<?> resourceClass);

	@Deprecated
	RegistryEntry addEntry(Class<?> clazz, RegistryEntry entry);

	@Deprecated
	ServiceUrlProvider getServiceUrlProvider();

	/**
	 * @param resourceInformation
	 * @return url for the given resourceInformation. Depending on the ServiceUrlProvider setup, a request must be active
	 * to invoke this method (to obtain domain/host information).
	 */
	String getResourceUrl(ResourceInformation resourceInformation);

	/**
	 * Retrieves the url of the resource
	 *
	 * @param resource Resource
	 * @return Url of provided resource in case it's a registered resource
	 */
	String getResourceUrl(Object resource);

	/**
	 * @deprecated use {{@link #getEntry(Class)}}
	 */
	@Deprecated
	RegistryEntry getEntryForClass(Class<?> resourceClass);

	/**
	 * @return ResourceInformation of the the top most super type of the provided resource.
	 */
	ResourceInformation getBaseResourceInformation(String resourceType);

}
