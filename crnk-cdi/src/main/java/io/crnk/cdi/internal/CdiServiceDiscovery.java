package io.crnk.cdi.internal;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.core.utils.Optional;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * CDI-based discovery of services.
 */
public class CdiServiceDiscovery implements ServiceDiscovery {

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getInstancesByType(Class<T> clazz) {
		BeanManager beanManager = CDI.current().getBeanManager();
		Set<Bean<?>> beans = beanManager.getBeans(clazz);
		List<T> list = new ArrayList<>();
		for (Bean<?> bean : beans) {
			CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);
			T object = (T) beanManager.getReference(bean, clazz, creationalContext);
			list.add(object);
		}
		return list;
	}

	@Override
	public <A extends Annotation> List<Object> getInstancesByAnnotation(Class<A> annotationClass) {
		BeanManager beanManager = CDI.current().getBeanManager();
		Set<Bean<?>> beans = beanManager.getBeans(Object.class);
		List<Object> list = new ArrayList<>();
		for (Bean<?> bean : beans) {
			Class<?> beanClass = bean.getBeanClass();
			Optional<A> annotation = ClassUtils.getAnnotation(beanClass, annotationClass);
			if (annotation.isPresent()) {
				CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);
				Object object = beanManager.getReference(bean, beanClass, creationalContext);
				list.add(object);
			}
		}
		return list;
	}

}
