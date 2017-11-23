package io.crnk.core.module;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.crnk.core.module.discovery.ResourceLookup;

class TestResourceLookup implements ResourceLookup {

	@Override
	public Set<Class<?>> getResourceClasses() {
		return new HashSet<Class<?>>(Arrays.asList(TestResource.class));
	}
}