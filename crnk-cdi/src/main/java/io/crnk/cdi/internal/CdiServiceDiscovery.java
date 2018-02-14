package io.crnk.cdi.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.TypeLiteral;

import io.crnk.core.engine.error.JsonApiExceptionMapper;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.module.discovery.ServiceDiscovery;
import java.util.Optional;

/**
 * CDI-based discovery of services.
 *
 * By default makes use of CDI.current() to get a BeanManager instance. An additional setter allows to set a custom
 * BeanManager.
 */
public class CdiServiceDiscovery implements ServiceDiscovery {


	private BeanManager beanManager;


	public void setBeanManager(BeanManager beanManager) {
		this.beanManager = beanManager;
	}

	public BeanManager getBeanManager() {
		if (beanManager != null) {
			return beanManager;
		}
		else {
			return CDI.current().getBeanManager();
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getInstancesByType(Class<T> clazz) {
		BeanManager beanManager = getBeanManager();

		Type type = clazz;
		if (clazz == JsonApiExceptionMapper.class) {
			TypeLiteral<JsonApiExceptionMapper<?>> typeLiteral = new TypeLiteral<JsonApiExceptionMapper<?>>() {
			};
			type = typeLiteral.getType();
		}

		Set<Bean<?>> beans = beanManager.getBeans(type);
		List<T> list = new ArrayList<>();
		for (Bean<?> bean : beans) {
			CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);
			T object = (T) beanManager.getReference(bean, type, creationalContext);
			list.add(object);
		}
		return list;
	}

	@Override
	public <A extends Annotation> List<Object> getInstancesByAnnotation(Class<A> annotationClass) {
		BeanManager beanManager = getBeanManager();
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
