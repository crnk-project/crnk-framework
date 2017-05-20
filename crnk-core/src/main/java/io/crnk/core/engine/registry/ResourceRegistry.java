package io.crnk.core.engine.registry;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.url.ServiceUrlProvider;

import java.util.Collection;

public interface ResourceRegistry {

	RegistryEntry addEntry(Class<?> clazz, RegistryEntry entry);

	boolean hasEntry(Class<?> clazz);

	RegistryEntry findEntry(Class<?> resourceClass);

	RegistryEntry getEntry(String resourceType);

	Collection<RegistryEntry> getResources();

	RegistryEntry findEntry(String type, Class<?> clazz);

	ServiceUrlProvider getServiceUrlProvider();

	String getResourceUrl(ResourceInformation resourceInformation);

	RegistryEntry getEntryForClass(Class<?> resourceClass);

	/**
	 * @param resourceType
	 * @return ResourceInformation of the the top most super type of the provided resource.
	 */
	ResourceInformation getBaseResourceInformation(String resourceType);

}
