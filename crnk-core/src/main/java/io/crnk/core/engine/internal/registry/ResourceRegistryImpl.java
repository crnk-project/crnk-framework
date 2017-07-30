package io.crnk.core.engine.internal.registry;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryPart;
import io.crnk.core.engine.url.ServiceUrlProvider;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.utils.Optional;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceRegistryImpl implements ResourceRegistry {

	private final ServiceUrlProvider serviceUrlProvider;

	private ModuleRegistry moduleRegistry;

	private ConcurrentHashMap<String, ResourceInformation> baseTypeCache = new ConcurrentHashMap<>();

	private ResourceRegistryPart rootPart;

	public ResourceRegistryImpl(ResourceRegistryPart rootPart, ModuleRegistry moduleRegistry, ServiceUrlProvider serviceUrlProvider) {
		this.rootPart = rootPart;
		this.moduleRegistry = moduleRegistry;
		this.serviceUrlProvider = serviceUrlProvider;
		this.moduleRegistry.setResourceRegistry(this);
	}


	/**
	 * Adds a new resource definition to a registry.
	 *
	 * @param resource      class of a resource
	 * @param registryEntry resource information
	 */
	public RegistryEntry addEntry(Class<?> resource, RegistryEntry registryEntry) {
		return addEntry(registryEntry);
	}


	protected RegistryEntry findEntry(Class<?> clazz, boolean allowNull) {
		Optional<Class<?>> resourceClazz = getResourceClass(clazz);
		if (allowNull && !resourceClazz.isPresent()) {
			return null;
		} else if (!resourceClazz.isPresent()) {
			throw new RepositoryNotFoundException(clazz.getCanonicalName());
		}
		return rootPart.getEntry(resourceClazz.get());
	}

	/**
	 * Searches the registry for a resource identified by a JSON API resource
	 * class. If a resource cannot be found,
	 *
	 * @param clazz resource type
	 * @return registry entry
	 */
	public RegistryEntry findEntry(Class<?> clazz) {
		return findEntry(clazz, false);
	}


	public Optional<Class<?>> getResourceClass(Class<?> resourceClass) {
		Class<?> currentClass = resourceClass;
		while (currentClass != null && currentClass != Object.class) {
			RegistryEntry entry = rootPart.getEntry(currentClass);
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
	 * @deprecated use {@link #getEntry(Class)}
	 */
	@Deprecated
	public RegistryEntry getEntryForClass(Class<?> resourceClass) {
		return getEntry(resourceClass);
	}


	public Optional<Class<?>> getResourceClass(Object resource) {
		return getResourceClass(resource.getClass());
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

	@Override
	public RegistryEntry addEntry(RegistryEntry entry) {
		return rootPart.addEntry(entry);
	}

	@Override
	public boolean hasEntry(Class<?> clazz) {
		return rootPart.hasEntry(clazz);
	}

	@Override
	public boolean hasEntry(String resourceType) {
		return rootPart.hasEntry(resourceType);
	}

	@Override
	public RegistryEntry getEntry(String resourceType) {
		return rootPart.getEntry(resourceType);
	}

	@Override
	public Collection<RegistryEntry> getResources() {
		return rootPart.getResources();
	}

	@Override
	public RegistryEntry getEntry(Class<?> clazz) {
		return rootPart.getEntry(clazz);
	}
}
