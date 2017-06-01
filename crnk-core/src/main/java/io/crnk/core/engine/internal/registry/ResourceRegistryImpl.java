package io.crnk.core.engine.internal.registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ServiceUrlProvider;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.core.exception.ResourceNotFoundInitializationException;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.utils.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceRegistryImpl implements ResourceRegistry {

	private final Map<String, RegistryEntry> resourcesByType;

	private final Map<Class, RegistryEntry> resourcesByClass;

	private final ServiceUrlProvider serviceUrlProvider;

	private final Logger logger = LoggerFactory.getLogger(ResourceRegistryImpl.class);

	private ModuleRegistry moduleRegistry;

	private ConcurrentHashMap<String, ResourceInformation> baseTypeCache = new ConcurrentHashMap<>();

	public ResourceRegistryImpl(ModuleRegistry moduleRegistry, ServiceUrlProvider serviceUrlProvider) {
		this.moduleRegistry = moduleRegistry;
		this.serviceUrlProvider = serviceUrlProvider;
		this.resourcesByType = new HashMap<>();
		this.resourcesByClass = new HashMap<>();
		this.moduleRegistry.setResourceRegistry(this);
	}

	/**
	 * Adds a new resource definition to a registry.
	 *
	 * @param resource class of a resource
	 * @param registryEntry resource information
	 */
	public RegistryEntry addEntry(Class<?> resource, RegistryEntry registryEntry) {
		resourcesByClass.put(resource, registryEntry);
		resourcesByType.put(registryEntry.getResourceInformation().getResourceType(), registryEntry);
		registryEntry.initialize(moduleRegistry);
		logger.debug("Added resource {} to ResourceRegistry", resource.getName());
		return registryEntry;
	}

	/**
	 * Searches the registry for a resource identified by a JSON API resource
	 * type. If a resource cannot be found, <i>null</i> is returned.
	 *
	 * @param resourceType resource type
	 * @return registry entry or <i>null</i>
	 */
	public RegistryEntry getEntry(String resourceType) {
		return resourcesByType.get(resourceType);
	}

	/**
	 * Searches the registry for a resource identified by a JSON API resource
	 * class. If a resource cannot be found,
	 * {@link ResourceNotFoundInitializationException} is thrown.
	 *
	 * @param clazz resource type
	 * @return registry entry
	 * @throws ResourceNotFoundInitializationException if resource is not found
	 */
	public RegistryEntry findEntry(Class<?> clazz) {
		return findEntry(clazz, false);
	}

	public boolean hasEntry(Class<?> clazz) {
		return findEntry(clazz, true) != null;
	}

	protected RegistryEntry findEntry(Class<?> clazz, boolean allowNull) {
		Optional<Class<?>> resourceClazz = getResourceClass(clazz);
		if (allowNull && !resourceClazz.isPresent()) {
			return null;
		}
		else if (!resourceClazz.isPresent()) {
			throw new RepositoryNotFoundException(clazz.getCanonicalName());
		}
		return resourcesByClass.get(resourceClazz.get());
	}

	public Optional<Class<?>> getResourceClass(Object resource) {
		return getResourceClass(resource.getClass());
	}

	public Optional<Class<?>> getResourceClass(Class<?> resourceClass) {
		Class<?> currentClass = resourceClass;
		while (currentClass != null && currentClass != Object.class) {
			RegistryEntry entry = resourcesByClass.get(currentClass);
			if (entry != null) {
				return (Optional) Optional.of(currentClass);
			}
			currentClass = currentClass.getSuperclass();
		}
		return Optional.empty();
	}

	public ServiceUrlProvider getServiceUrlProvider() {
		return serviceUrlProvider;
	}

	/**
	 * Get a list of all registered resources by Crnk.
	 *
	 * @return resources
	 */
	public Set<RegistryEntry> getResources() {
		return Collections.unmodifiableSet(new HashSet<>(resourcesByType.values()));
	}

	/**
	 * @deprecated use {@link #getEntry(Class)}
	 */
	@Deprecated
	public RegistryEntry getEntryForClass(Class<?> resourceClass) {
		return getEntry(resourceClass);
	}

	public RegistryEntry getEntry(Class<?> resourceClass) {
		return resourcesByClass.get(resourceClass);
	}

	@Override
	public String getResourceUrl(ResourceInformation resourceInformation) {
		String url = UrlUtils.removeTrailingSlash(serviceUrlProvider.getUrl());
		return url + "/" + resourceInformation.getResourceType();
	}

	@Override
	public ResourceInformation getBaseResourceInformation(String resourceType) {
		ResourceInformation baseInformation = baseTypeCache.get(resourceType);
		if (baseInformation != null) {
			return baseInformation;
		}

		RegistryEntry entry = getEntry(resourceType);
		baseInformation = entry.getResourceInformation();
		while (baseInformation.getSuperResourceType() != null) {
			entry = getEntry(baseInformation.getSuperResourceType());
			baseInformation = entry.getResourceInformation();
		}

		baseTypeCache.put(resourceType, baseInformation);
		return baseInformation;
	}
}
