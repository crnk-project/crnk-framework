package io.crnk.core.module.discovery;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

public class TestServiceDiscovery implements ServiceDiscovery {

	@Override
	public <T> List<T> getInstancesByType(Class<T> clazz) {
		return Collections.emptyList();
	}

	@Override
	public <A extends Annotation> List<Object> getInstancesByAnnotation(Class<A> annotation) {
		return Collections.emptyList();
	}
}
