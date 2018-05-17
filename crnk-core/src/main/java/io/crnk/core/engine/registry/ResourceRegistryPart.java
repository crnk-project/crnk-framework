package io.crnk.core.engine.registry;

import java.util.Collection;


public interface ResourceRegistryPart {

	RegistryEntry addEntry(RegistryEntry entry);

	boolean hasEntry(Class<?> clazz);

	boolean hasEntry(String resourceType);

	RegistryEntry getEntry(String resourceType);

	Collection<RegistryEntry> getResources();

	RegistryEntry getEntry(Class<?> clazz);

	RegistryEntry getEntryByPath(String resourcePath);

	void addListener(ResourceRegistryPartListener listener);

	void removeListener(ResourceRegistryPartListener listener);

}
