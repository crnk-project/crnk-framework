package io.crnk.core.engine.internal.dispatcher.path;

import java.io.Serializable;
import java.util.List;

import io.crnk.core.engine.registry.RegistryEntry;

/**
 * Represents a part of a path which relate a field of a resource e.g. for /resource/1 will be represented as
 * an object of ResourcePath type.
 */
public class ResourcePath extends JsonPath {


	public ResourcePath(RegistryEntry rootEntry, List<Serializable> ids) {
		super(rootEntry, ids);
	}

	@Override
	public boolean isCollection() {
		return getIds() == null || getIds().size() > 1;
	}
}
