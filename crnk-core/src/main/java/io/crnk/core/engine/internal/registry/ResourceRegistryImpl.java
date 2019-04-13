package io.crnk.core.engine.internal.registry;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.query.QueryContext;
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

		setRootPart(rootPart);
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

	public String getResourceUrl(ResourceInformation resourceInformation) {
		String url = UrlUtils.removeTrailingSlash(getServiceUrlProvider().getUrl());
		if (url == null) {
			return null;
		}
		if (resourceInformation.isNested()) {
			throw new UnsupportedOperationException("method not available for nested resources since id of parent needed");
		}
		String resourcePath = resourceInformation.getResourcePath();
		return url != null ? String.format("%s/%s", url, resourcePath) : null;
	}

	public String getResourceUrl(final Class<?> clazz) {
		RegistryEntry registryEntry = findEntry(clazz);

		return getResourceUrl(registryEntry.getResourceInformation());
	}

	@Override
	public String getResourceUrl(QueryContext queryContext, ResourceInformation resourceInformation) {
		String baseUrl = queryContext != null ? queryContext.getBaseUrl() : getServiceUrlProvider().getUrl();
		String url = UrlUtils.removeTrailingSlash(baseUrl);
		String resourcePath = resourceInformation.getResourcePath();
		return url != null ? String.format("%s/%s", url, resourcePath) : null;
	}

	public String getResourceUrl(final Object resource) {
		return getResourceUrl(null, resource);
	}

	public String getResourceUrl(final Class<?> clazz, final String id) {
		RegistryEntry registryEntry = findEntry(clazz);
		String typeUrl = getResourceUrl(registryEntry.getResourceInformation());
		return typeUrl != null ? String.format("%s/%s", typeUrl, id) : null;
	}

	@Override
	public String getResourceUrl(QueryContext queryContext, final Object resource) {
		Optional<Class<?>> type = getResourceClass(resource);
		if (!type.isPresent()) {
			throw new InvalidResourceException("Not registered resource found: " + resource);
		}
		ResourceInformation resourceInformation = findEntry(type.get()).getResourceInformation();
		Object id = resourceInformation.getId(resource);
		return getResourceUrl(queryContext, resourceInformation, id);
	}

	@Override
	public String getResourceUrl(QueryContext queryContext, ResourceInformation resourceInformation, Object id) {
		if (resourceInformation.isNested()) {
			ResourceField parentField = resourceInformation.getParentField();

			RegistryEntry parentEntry = getEntry(parentField.getOppositeResourceType());
			ResourceInformation parentInformation = parentEntry.getResourceInformation();
			ResourceField childrenField = parentInformation.findRelationshipFieldByName(parentField.getOppositeName());

			if(resourceInformation.isSingularNesting()){
				String parentUrl = getResourceUrl(queryContext, parentInformation) + "/" + parentInformation.toIdString(id);
				return parentUrl + "/" + childrenField.getJsonName();
			}else{
				Object parentId = resourceInformation.getParentIdAccessor().getValue(id);
				Object nestedId = resourceInformation.getChildIdAccessor().getValue(id);
				PreconditionUtil.verify(parentId != null, "nested resources must have a parent, got null from " + parentField.getIdName());
				PreconditionUtil.verify(nestedId != null, "nested resources must have a non-null identifier");
				String parentUrl = getResourceUrl(queryContext, parentInformation) + "/" + parentInformation.toIdString(parentId);

				TypeParser typeParser = moduleRegistry.getTypeParser();
				return parentUrl + "/" + childrenField.getJsonName() + "/" + typeParser.toString(nestedId);
			}
		}

		return String.format("%s/%s", getResourceUrl(queryContext, resourceInformation), resourceInformation.toIdString(id));

	}


	@Override
	public String getResourceUrl(QueryContext queryContext, final Class<?> clazz) {
		RegistryEntry registryEntry = findEntry(clazz);
		return getResourceUrl(queryContext, registryEntry.getResourceInformation());
	}

	@Override
	public String getResourceUrl(QueryContext queryContext, final Class<?> clazz, final String id) {
		RegistryEntry registryEntry = findEntry(clazz);
		ResourceInformation resourceInformation = registryEntry.getResourceInformation();
		String typeUrl = getResourceUrl(queryContext, resourceInformation);
		return typeUrl != null ? String.format("%s/%s", typeUrl, id) : null;
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
			String entryResourceType = entry.getResourceInformation().getResourceType();
			entry = getEntry(superResourceType);
			PreconditionUtil.verify(entry != null, "superType=%s not found for resourceType=%s", superResourceType, entryResourceType);
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
	public boolean hasEntry(Type type) {
		return rootPart.hasEntry(type);
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
	public RegistryEntry getEntry(Class<?> clazz) {
		return rootPart.getEntry(clazz);
	}

	@Override
	public RegistryEntry getEntry(Type type) {
		return rootPart.getEntry(type);
	}

	@Override
	public RegistryEntry getEntryByPath(String resourcePath) {
		return rootPart.getEntryByPath(resourcePath);
	}

	@Override
	public Collection<RegistryEntry> getEntries() {
		return rootPart.getEntries();
	}

	public void setRootPart(ResourceRegistryPart rootPart) {
		if (this.rootPart != null) {
			this.rootPart.removeListener(rootListener);
		}
		if (rootPart != null) {
			rootPart.addListener(rootListener);
		}
		this.rootPart = rootPart;
	}
}
