package io.crnk.core.engine.registry;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements ResourceRegistryPart by maintaining a simple set of RegistryEntry in memory.
 */
public class DefaultResourceRegistryPart extends ResourceRegistryPartBase {

	private final Logger logger = LoggerFactory.getLogger(DefaultResourceRegistryPart.class);

	private final Map<String, RegistryEntry> resourcesByType;

	private final Map<String, RegistryEntry> resourcesByPath;

	private final Map<Type, RegistryEntry> resourcesByImplementationType;

	public DefaultResourceRegistryPart() {
		this.resourcesByType = new HashMap<>();
		this.resourcesByPath = new HashMap<>();
		this.resourcesByImplementationType = new HashMap<>();
	}

	@Override
	public RegistryEntry addEntry(RegistryEntry entry) {
		ResourceInformation resourceInformation = entry.getResourceInformation();
		Type implementationType = resourceInformation.getImplementationType();
		String resourceType = resourceInformation.getResourceType();
		String resourcePath = resourceInformation.getResourcePath();
		PreconditionUtil.verify(resourceType != null, "no resourceType set for entry %d", entry);
		PreconditionUtil
				.verify(!resourcesByType.containsKey(resourceType), "resourceType '%s' already exists, cannot add entry %s",
						resourceType, entry);
		if (entry.hasResourceRepository()) {
			PreconditionUtil
					.verify(!resourcesByPath.containsKey(resourcePath), "resourceType '%s' already exists, cannot add entry %s",
							resourcePath, entry);
			resourcesByPath.put(resourcePath != null ? resourcePath : resourceType, entry);
		}
		resourcesByImplementationType.put(implementationType, entry);
		resourcesByType.put(resourceType, entry);
		logger.debug("Added resource '{}' to ResourceRegistry", resourceType);
		notifyChange();
		return entry;
	}

	@Override
	public boolean hasEntry(Class<?> implementationClass) {
		return getEntry(implementationClass) != null;
	}

	@Override
	public boolean hasEntry(Type implementationType) {
		return getEntry(implementationType) != null;
	}

	@Override
	public boolean hasEntry(String resourceType) {
		return this.getEntry(resourceType) != null;
	}

	@Override
	public RegistryEntry getEntry(Class<?> implementationClass) {
		return resourcesByImplementationType.get(implementationClass);
	}

	@Override
	public RegistryEntry getEntry(Type implementationType) {
		return resourcesByImplementationType.get(implementationType);
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
	 * path. If a resource cannot be found, <i>null</i> is returned.
	 *
	 * @param resourcePath resource path
	 * @return registry entry or <i>null</i>
	 */
	public RegistryEntry getEntryByPath(String resourcePath) {
		return resourcesByPath.get(resourcePath);
	}

}
