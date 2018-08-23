package io.crnk.core.engine.registry;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collection;


public interface ResourceRegistryPart {

    default RegistryEntry getEntryByPath(String path, int version) {
        //noop;
        //throw new NotImplementedException();
		return null;
    }

    default RegistryEntry getEntry(Class<?> clazz, int version) {
        //noop;
		//throw new NotImplementedException();
		return null;
    }

    default RegistryEntry getEntry(String resourceType, int version) {
        //noop;
		//throw new NotImplementedException();
		return null;
    }


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
