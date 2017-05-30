package io.crnk.core.engine.registry;

import java.util.Collection;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.url.ServiceUrlProvider;

public interface ResourceRegistry {

	RegistryEntry addEntry(Class<?> clazz, RegistryEntry entry);

	boolean hasEntry(Class<?> clazz);

	RegistryEntry getEntry(String resourceType);

	RegistryEntry findEntry(Class<?> resourceClass);

	Collection<RegistryEntry> getResources();

	RegistryEntry getEntry(Class<?> clazz);

	ServiceUrlProvider getServiceUrlProvider();

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
