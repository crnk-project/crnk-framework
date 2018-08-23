package io.crnk.core.engine.registry;

import io.crnk.core.engine.information.resource.ResourceInformation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VersionedResourceRegistryPart extends ResourceRegistryPartBase {

    private List<RegistryEntryInformation> registryEntryInformations;

    public VersionedResourceRegistryPart() {
        this.registryEntryInformations = new ArrayList<>();
    }

    @Override
    public RegistryEntry addEntry(RegistryEntry entry) {
        ResourceInformation resourceInformation = entry.getResourceInformation();
        Class<?> resourceClass = resourceInformation.getResourceClass();
        String resourceType = resourceInformation.getResourceType();
        String resourcePath = resourceInformation.getResourcePath();

        final int version = obtainVersion(entry);

        RegistryEntryInformation registryEntryInformation
                = new RegistryEntryInformation(resourcePath, resourceType, resourceClass, version, entry);

        registryEntryInformations.add(registryEntryInformation);
        notifyChange();
        return entry;
    }

	private int obtainVersion(RegistryEntry entry) {
		if (entry.getRepositoryInformation() == null) {
			return 0;
		} else {
			return entry.getRepositoryInformation().getVersion();
		}
	}

	public boolean hasEntry(Class<?> clazz) {
        return getEntry(clazz) != null;
    }

    @Override
    public boolean hasEntry(String resourceType) {
        return this.getEntry(resourceType) != null;
    }

    public RegistryEntry getEntry(Class<?> resourceClass) {
        return registryEntryInformations.stream()
                                        .filter(registryEntryInformation -> registryEntryInformation.getClazz().equals(resourceClass))
                                        .map(RegistryEntryInformation::getRegistryEntry)
                                        .findFirst()
                                        .orElse(null);
    }

    /**
     * Get a list of all registered resources by Crnk.
     *
     * @return resources
     */
    public Set<RegistryEntry> getResources() {
        return registryEntryInformations.stream()
                                        .map(RegistryEntryInformation::getRegistryEntry)
                                        .collect(Collectors.toSet());
    }


    /**
     * Searches the registry for a resource identified by a JSON API resource
     * type. If a resource cannot be found, <i>null</i> is returned.
     *
     * @param resourceType resource type
     * @return registry entry or <i>null</i>
     */
    public RegistryEntry getEntry(String resourceType) {
        return registryEntryInformations.stream()
                                        .filter(registryEntryInformation -> registryEntryInformation.getType().equals(resourceType))
                                        .map(RegistryEntryInformation::getRegistryEntry)
                                        .findFirst()
                                        .orElse(null);
    }

    /**
     * Searches the registry for a resource identified by a JSON API resource
     * path. If a resource cannot be found, <i>null</i> is returned.
     *
     * @param resourcePath resource path
     * @return registry entry or <i>null</i>
     */
    public RegistryEntry getEntryByPath(String resourcePath) {
        return registryEntryInformations.stream()
                                        .filter(registryEntryInformation -> registryEntryInformation.getType().equals(resourcePath))
                                        .map(RegistryEntryInformation::getRegistryEntry)
                                        .max(Comparator.comparingInt(o -> o.getRepositoryInformation()
                                                                           .getVersion()))
                                        .orElse(null);
    }

    @Override
    public RegistryEntry getEntryByPath(String resourcePath, int version) {
        if (version == 0) {
            return getEntryByPath(resourcePath);
        }

        return registryEntryInformations.stream()
                                        .filter(registryEntryInformation -> registryEntryInformation.getType().equals(resourcePath))
                                        .filter(registryEntryInformation -> registryEntryInformation.getVersion() == version)
                                        .map(RegistryEntryInformation::getRegistryEntry)
                                        .findFirst()
                                        .orElse(null);
    }

    @Override
    public RegistryEntry getEntry(Class<?> clazz, int version) {
        if (version == 0) {
            return getEntry(clazz);
        }

        return registryEntryInformations.stream()
                                        .filter(registryEntryInformation -> registryEntryInformation.getClazz().equals(clazz))
                                        .filter(registryEntryInformation -> registryEntryInformation.getVersion() == version)
                                        .map(RegistryEntryInformation::getRegistryEntry)
                                        .findFirst()
                                        .orElse(null);
    }


    @Override
    public RegistryEntry getEntry(String resourceType, int version) {
        if (version == 0) {
            return getEntry(resourceType);
        }

        return registryEntryInformations.stream()
                                        .filter(registryEntryInformation -> registryEntryInformation.getType().equals(resourceType))
                                        .filter(registryEntryInformation -> registryEntryInformation.getVersion() == version)
                                        .map(RegistryEntryInformation::getRegistryEntry)
                                        .findFirst()
                                        .orElse(null);
    }

    private class RegistryEntryInformation {
        private String path;
        private String type;
        private Class<?> clazz;
        private int version;
        private RegistryEntry registryEntry;

        public RegistryEntryInformation(String path, String type, Class<?> clazz, int version, RegistryEntry registryEntry) {
            this.path = path;
            this.type = type;
            this.clazz = clazz;
            this.version = version;
            this.registryEntry = registryEntry;
        }

        public String getPath() {
            return path;
        }

        public String getType() {
            return type;
        }

        public Class<?> getClazz() {
            return clazz;
        }

        public int getVersion() {
            return version;
        }

        public RegistryEntry getRegistryEntry() {
            return registryEntry;
        }
    }
}
