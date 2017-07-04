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

	@Deprecated
	String getResourceUrl(ResourceInformation resourceInformation);

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
