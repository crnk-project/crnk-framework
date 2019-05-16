package io.crnk.core.engine.registry;

import java.lang.reflect.Type;
import java.util.Collection;


public interface ResourceRegistryPart {

	RegistryEntry addEntry(RegistryEntry entry);

	boolean hasEntry(Class<?> clazz);

	boolean hasEntry(Type type);

	boolean hasEntry(String resourceType);

	RegistryEntry getEntry(String resourceType);

	Collection<RegistryEntry> getEntries();

	RegistryEntry getEntry(Class<?> clazz);

	RegistryEntry getEntry(Type type);

	RegistryEntry getEntryByPath(String resourcePath);

	void addListener(ResourceRegistryPartListener listener);

	void removeListener(ResourceRegistryPartListener listener);

}
