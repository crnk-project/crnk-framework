package io.crnk.core.engine.registry;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryContext;
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
	 * Retrieves the url of the type
	 *
	 * @param clazz Type
	 * @return Url of provided resource in case it's a registered resource
	 */
	String getResourceUrl(Class<?> clazz);

	/**
	 * Retrieves the url of the type and identifier
	 *
	 * @param clazz Type
	 * @param id    Identifier
	 * @return Url of provided resource in case it's a registered resource
	 */
	String getResourceUrl(Class<?> clazz, String id);


	/**
	 * @param resourceInformation
	 * @return url for the given resourceInformation. Depending on the ServiceUrlProvider setup, a request must be active
	 * to invoke this method (to obtain domain/host information).
	 */
	String getResourceUrl(QueryContext queryContext, ResourceInformation resourceInformation);

	String getResourceUrl(QueryContext queryContext, ResourceInformation resourceInformation, Object id);

	/**
	 * Retrieves the url of the resource
	 *
	 * @param resource Resource
	 * @return Url of provided resource in case it's a registered resource
	 */
	String getResourceUrl(QueryContext queryContext, Object resource);

	/**
	 * Retrieves the url of the type
	 *
	 * @param clazz Type
	 * @return Url of provided resource in case it's a registered resource
	 */
	String getResourceUrl(QueryContext queryContext, Class<?> clazz);

	/**
	 * Retrieves the url of the type and identifier
	 *
	 * @param clazz Type
	 * @param id    Identifier
	 * @return Url of provided resource in case it's a registered resource
	 */
	String getResourceUrl(QueryContext queryContext, Class<?> clazz, String id);


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
