package io.crnk.core.engine.registry;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements a hierarchical {@link ResourceRegistryPart} by maintaining a list of child ResourceRegistryPart.
 */
public class HierarchicalResourceRegistryPart extends ResourceRegistryPartBase {

	private static final String PATH_SEPARATOR = "/";

	private Map<String, ResourceRegistryPart> partMap = new HashMap<>();

	private List<ResourceRegistryPart> partList = new ArrayList<>();

	private ResourceRegistryPartListener childListener = new ResourceRegistryPartListener() {
		@Override
		public void onChanged(ResourceRegistryPartEvent event) {
			notifyChange();
		}
	};

	public void putPart(String prefix, ResourceRegistryPart part) {
		if (partMap.containsKey(prefix)) {
			throw new IllegalStateException("part with prefx " + prefix + " already exists");
		}
		partMap.put(prefix, part);
		partList.add(part);
		part.addListener(childListener);
	}


	@Override
	public RegistryEntry addEntry(RegistryEntry entry) {
		String resourceType = entry.getResourceInformation().getResourceType();
		ResourceRegistryPart part = getPart(resourceType);
		if (part == null) {
			throw new IllegalStateException("cannot add " + resourceType + ", no part available in hierarchy");
		}
		return part.addEntry(entry);
	}

	@Override
	public boolean hasEntry(Class<?> implementationClass) {
		for (ResourceRegistryPart part : partList) {
			if (part.hasEntry(implementationClass)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasEntry(Type implementationType) {
		for (ResourceRegistryPart part : partList) {
			if (part.hasEntry(implementationType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasEntry(String resourceType) {
		for (ResourceRegistryPart part : partList) {
			if (part.hasEntry(resourceType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public RegistryEntry getEntry(String resourceType) {
		ResourceRegistryPart part = getPart(resourceType);
		if (part == null) {
			return null;
		}
		return part.getEntry(resourceType);
	}

	@Override
	public RegistryEntry getEntryByPath(String resourcePath) {
		ResourceRegistryPart part = getPart(resourcePath);
		if (part == null) {
			return null;
		}
		return part.getEntryByPath(resourcePath);
	}

	private ResourceRegistryPart getPart(String resourceType) {
		int sep = resourceType.indexOf(PATH_SEPARATOR);
		String prefix;
		if (sep == -1) {
			prefix = "";
		} else {
			prefix = resourceType.substring(0, sep);
		}
		return partMap.get(prefix);
	}

	@Override
	public Collection<RegistryEntry> getResources() {
		List<RegistryEntry> list = new ArrayList<>();
		for (ResourceRegistryPart part : partList) {
			list.addAll(part.getResources());
		}
		return list;
	}

	@Override
	public RegistryEntry getEntry(Class<?> implementationClass) {
		for (ResourceRegistryPart part : partList) {
			if (part.hasEntry(implementationClass)) {
				return part.getEntry(implementationClass);
			}
		}
		return null;
	}

	@Override
	public RegistryEntry getEntry(Type implementationType) {
		for (ResourceRegistryPart part : partList) {
			if (part.hasEntry(implementationType)) {
				return part.getEntry(implementationType);
			}
		}
		return null;
	}
}
