package io.crnk.core.engine.registry;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.exception.RepositoryNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Implements ResourceRegistryPart by maintaining a simple set of RegistryEntry in memory.
 */
public class DefaultResourceRegistryPart extends ResourceRegistryPartBase {

    private final Logger logger = LoggerFactory.getLogger(DefaultResourceRegistryPart.class);

    private final Map<String, RegistryEntry> resourcesByType;

    private final Map<String, RegistryEntry> resourcesByPath;

    private final Map<Type, RegistryEntry> resourcesByImplementationType;

    private int latestVersion;

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
        PreconditionUtil.verify(resourceType != null, "no resourceType set for entry %s", entry);
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

        latestVersion = Math.max(latestVersion, ignoreUnbounded(resourceInformation.getVersionRange().getMax()));
        for (ResourceField field : resourceInformation.getFields()) {
            latestVersion = Math.max(latestVersion, ignoreUnbounded(field.getVersionRange().getMax()));
        }

        logger.debug("Added resource '{}' to ResourceRegistry", resourceType);
        notifyChange();
        return entry;
    }

    private int ignoreUnbounded(int version) {
        return version != Integer.MAX_VALUE ? version : 0;
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
        return resourcesByType.get(resourceType) != null;
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
    public Set<RegistryEntry> getEntries() {
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
        RegistryEntry entry = resourcesByType.get(resourceType);
        if (entry == null) {
            throw new RepositoryNotFoundException(resourceType, resourcesByType.keySet());
        }
        return entry;
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

    @Override
    public int getLatestVersion() {
        return latestVersion;
    }

}
