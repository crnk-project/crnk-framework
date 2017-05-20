package io.crnk.spring.internal;

import io.crnk.core.module.discovery.ServiceDiscovery;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring-based discovery of services.
 */
public class SpringServiceDiscovery implements ServiceDiscovery, ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public <T> List<T> getInstancesByType(Class<T> clazz) {
		return new ArrayList<>(applicationContext.getBeansOfType(clazz).values());
	}

	@Override
	public <A extends Annotation> List<Object> getInstancesByAnnotation(Class<A> annotationClass) {
		return new ArrayList<>(applicationContext.getBeansWithAnnotation(annotationClass).values());
	}
}
