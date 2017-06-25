package io.crnk.core.engine.registry;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.registry.ResourceRegistryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Implements ResourceRegistryPart by maintaining a simple set of RegistryEntry in memory.
 */
public class DefaultResourceRegistryPart implements ResourceRegistryPart {

	private final Logger logger = LoggerFactory.getLogger(ResourceRegistryImpl.class);

	private final Map<String, RegistryEntry> resourcesByType;

	private final Map<Class, RegistryEntry> resourcesByClass;

	public DefaultResourceRegistryPart() {
		this.resourcesByType = new HashMap<>();
		this.resourcesByClass = new HashMap<>();
	}

	@Override
	public RegistryEntry addEntry(RegistryEntry entry) {
		ResourceInformation resourceInformation = entry.getResourceInformation();
		Class<?> resourceClass = resourceInformation.getResourceClass();
		resourcesByClass.put(resourceClass, entry);
		resourcesByType.put(resourceInformation.getResourceType(), entry);
		logger.debug("Added resource {} to ResourceRegistry", entry.getResourceInformation().getResourceType());
		return entry;
	}

	public boolean hasEntry(Class<?> clazz) {
		return getEntry(clazz) != null;
	}

	@Override
	public boolean hasEntry(String resourceType) {
		return this.getEntry(resourceType) != null;
	}

	public RegistryEntry getEntry(Class<?> resourceClass) {
		return resourcesByClass.get(resourceClass);
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
}
