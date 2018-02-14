package io.crnk.core.engine.internal.registry;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryPart;
import io.crnk.core.engine.registry.ResourceRegistryPartBase;
import io.crnk.core.engine.registry.ResourceRegistryPartEvent;
import io.crnk.core.engine.registry.ResourceRegistryPartListener;
import io.crnk.core.engine.url.ServiceUrlProvider;
import io.crnk.core.exception.InvalidResourceException;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.core.module.ModuleRegistry;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceRegistryImpl extends ResourceRegistryPartBase implements ResourceRegistry {

	private ModuleRegistry moduleRegistry;

	private ConcurrentHashMap<String, ResourceInformation> baseTypeCache = new ConcurrentHashMap<>();

	private ResourceRegistryPart rootPart;

	private ResourceRegistryPartListener rootListener = new ResourceRegistryPartListener() {
		@Override
		public void onChanged(ResourceRegistryPartEvent event) {
			notifyChange();
		}
	};

	public ResourceRegistryImpl(ResourceRegistryPart rootPart, ModuleRegistry moduleRegistry) {
		this.rootPart = rootPart;
		this.moduleRegistry = moduleRegistry;
		this.moduleRegistry.setResourceRegistry(this);

		rootPart.addListener(rootListener);
	}

	/**
	 * Adds a new resource definition to a registry.
	 *
	 * @param resource class of a resource
	 * @param registryEntry resource information
	 */
	public RegistryEntry addEntry(Class<?> resource, RegistryEntry registryEntry) {
		return addEntry(registryEntry);
	}

	protected RegistryEntry findEntry(Class<?> clazz, boolean allowNull) {
		Optional<Class<?>> resourceClazz = getResourceClass(clazz);
		if (allowNull && !resourceClazz.isPresent()) {
			return null;
		}
		else if (!resourceClazz.isPresent()) {
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
		return moduleRegistry.getHttpRequestContextProvider().getServiceUrlProvider();
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
		String url = UrlUtils.removeTrailingSlash(getServiceUrlProvider().getUrl());
		return String.format("%s/%s", url, resourceInformation.getResourceType());
	}

	@Override
	public String getResourceUrl(final Object resource) {
		Optional<Class<?>> type = getResourceClass(resource);
		if (type.isPresent()) {
			ResourceInformation resourceInformation = findEntry(type.get()).getResourceInformation();
			return String.format("%s/%s", getResourceUrl(resourceInformation), resourceInformation.getId(resource));
		}

		throw new InvalidResourceException("Not registered resource found: " + resource);
	}

	@Override
	public String getResourceUrl(final Class<?> clazz) {
		RegistryEntry registryEntry = findEntry(clazz);

		return getResourceUrl(registryEntry.getResourceInformation());
	}

	@Override
	public String getResourceUrl(final Class<?> clazz, final String id) {
		RegistryEntry registryEntry = findEntry(clazz);

		return String.format("%s/%s", getResourceUrl(registryEntry.getResourceInformation()), id);
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
			String superResourceType = baseInformation.getSuperResourceType();
			entry = getEntry(superResourceType);
			PreconditionUtil.assertNotNull(superResourceType, entry);
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
