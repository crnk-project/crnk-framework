package io.crnk.guice;

import com.google.inject.Injector;
import com.google.inject.Key;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.module.discovery.ServiceDiscovery;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Guice-based discovery of services.
 */
public class GuiceServiceDiscovery implements ServiceDiscovery {

	private Injector injector;

	public GuiceServiceDiscovery(Injector injector) {
		this.injector = injector;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getInstancesByType(Class<T> clazz) {
		List<T> instances = new ArrayList<>();
		for (Key<?> key : injector.getAllBindings().keySet()) {
			if (clazz.isAssignableFrom(key.getTypeLiteral().getRawType())) {
				T instance = (T) injector.getInstance(key);
				instances.add(instance);
			}
		}
		return instances;
	}

	@Override
	public <A extends Annotation> List<Object> getInstancesByAnnotation(Class<A> annotationClass) {
		List<Object> instances = new ArrayList<>();
		for (Key<?> key : injector.getAllBindings().keySet()) {
			Class<?> beanClass = key.getTypeLiteral().getRawType();
			Optional<A> annotation = ClassUtils.getAnnotation(beanClass, annotationClass);
			if (annotation.isPresent()) {
				Object instance = injector.getInstance(key);
				instances.add(instance);
			}
		}
		return instances;
	}

}
