package io.crnk.core.module.discovery;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.crnk.core.module.Module;

/**
 * Combines all {@link ResourceLookup} instances provided by the registered
 * {@link Module}.
 */
public class MultiResourceLookup implements ResourceLookup {

	private Collection<ResourceLookup> lookups;

	public MultiResourceLookup(List<ResourceLookup> lookups) {
		this.lookups = lookups;
	}

	@Override
	public Set<Class<?>> getResourceClasses() {
		Set<Class<?>> set = new HashSet<>();
		for (ResourceLookup lookup : lookups) {
			set.addAll(lookup.getResourceClasses());
		}
		return set;
	}
}