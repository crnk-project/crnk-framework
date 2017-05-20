package io.crnk.core.module.discovery;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Used to integrate Crnk into various dependency management frameworks and other systems.
 */
public interface ServiceDiscovery {

	<T> List<T> getInstancesByType(Class<T> clazz);

	<A extends Annotation> List<Object> getInstancesByAnnotation(Class<A> annotation);
}
