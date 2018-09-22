package io.crnk.core.engine.internal.document.mapper;

import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.registry.ResourceRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Cache resource/field pairs already populated to avoid loops
 */
class IncludePopulatedCache {

	private final ResourceRegistry resourceRegistry;

	private HashSet<String> processed = new HashSet<>();

	public IncludePopulatedCache(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
	}

	public void markProcessed(Resource resource, ResourceField field) {
		String key = getKey(resource, field);
		processed.add(key);
	}

	public Collection<Resource> filterProcessed(Collection<Resource> resources, ResourceField field) {
		Collection<Resource> result = new ArrayList<>();
		for (Resource resource : resources) {
			if (!wasProcessed(resource, field)) {
				result.add(resource);
				markProcessed(resource, field);
			}
		}
		return result;
	}

	public boolean wasProcessed(Resource resource, ResourceField field) {
		String key = getKey(resource, field);
		return processed.contains(key);
	}

	private String getKey(Resource resource, ResourceField field) {
		return resourceRegistry.getBaseResourceInformation(resource.getType()).getResourceType() + "@" + resource.getId()
				+ "@" + field.getUnderlyingName();
	}
}