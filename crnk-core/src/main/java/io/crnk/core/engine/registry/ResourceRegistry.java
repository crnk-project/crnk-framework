package io.crnk.core.engine.registry;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.url.ServiceUrlProvider;
import io.crnk.core.resource.annotations.JsonApiVersion;

/**
 * ${@link ResourceRegistryPart} implementation if a number of convenience methods used and exposed
 * trough the Module API.
 */
public interface ResourceRegistry extends ResourceRegistryPart {

    RegistryEntry findEntry(Class<?> resourceClass);

    /**
     * @return entry matching the given resource, accoutring for the {@link Class}, {@link io.crnk.core.resource.annotations.JsonApiId},
     * {@link io.crnk.core.engine.document.Resource}.
     */
    RegistryEntry findEntry(Object resource);

    /**
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
     * Retrieves the path of the type and identifier
     *
     * @param id Identifier
     * @return complete path of provided resource in case it's a registered resource
     */
    String getResourcePath(ResourceInformation resourceInformation, Object id);

    /**
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
     * @return ResourceInformation of the the top most super type of the provided resource.
     */
    ResourceInformation getBaseResourceInformation(String resourceType);

    ServiceUrlProvider getServiceUrlProvider();

    /**
     * @return latest version within repository or 0 if no versioning in place. For more information
     * see {@link JsonApiVersion}
     */
    int getLatestVersion();

}
