package io.crnk.core.module.discovery;

import io.crnk.core.module.Module;

import java.util.*;

/**
 * Combines all {@link ResourceLookup} instances provided by the registered
 * {@link Module}.
 */
public class MultiResourceLookup implements ResourceLookup {

	private Collection<ResourceLookup> lookups;

	public MultiResourceLookup(List<ResourceLookup> lookups) {
		this.lookups = lookups;
	}

	public static ResourceLookup newInstance(ResourceLookup... lookups) {
		List<ResourceLookup> list = new ArrayList<>();
		for (ResourceLookup lookup : lookups) {
			if (lookup != null) {
				list.add(lookup);
			}
		}
		return new MultiResourceLookup(list);
	}

	@Override
	public Set<Class<?>> getResourceClasses() {
		Set<Class<?>> set = new HashSet<>();
		for (ResourceLookup lookup : lookups) {
			set.addAll(lookup.getResourceClasses());
		}
		return set;
	}

	@Override
	public Set<Class<?>> getResourceRepositoryClasses() {
		Set<Class<?>> set = new HashSet<>();
		for (ResourceLookup lookup : lookups) {
			set.addAll(lookup.getResourceRepositoryClasses());
		}
		return set;
	}
}