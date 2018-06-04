package io.crnk.cdi.internal;

import io.crnk.core.engine.error.JsonApiExceptionMapper;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.core.utils.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.TypeLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * CDI-based discovery of services.
 * <p>
 * By default makes use of CDI.current() to get a BeanManager instance. An additional setter allows to set a custom
 * BeanManager.
 */
public class CdiServiceDiscovery implements ServiceDiscovery {

	private static final Logger LOGGER = LoggerFactory.getLogger(CdiServiceDiscovery.class);


	private BeanManager beanManager;

	private Boolean cdiAvailable = null;

	public void setBeanManager(BeanManager beanManager) {
		this.beanManager = beanManager;
	}

	public BeanManager getBeanManager() {
		if (beanManager != null) {
			return beanManager;
		} else if (cdiAvailable != Boolean.FALSE) {
			try {
				CDI<Object> current = CDI.current();
				cdiAvailable = Boolean.TRUE;
				return current.getBeanManager();
			} catch (IllegalStateException e) {
				LOGGER.error("CDI context not available, CdiServiceDiscovery will not be used");
				LOGGER.debug("CDI.current() failed", e);
				cdiAvailable = Boolean.FALSE;
				return null;
			}
		}
		return null;
	}


	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getInstancesByType(Class<T> clazz) {
		BeanManager beanManager = getBeanManager();
		List<T> list = new ArrayList<>();
		if (beanManager != null) {
			Type type = clazz;
			if (clazz == JsonApiExceptionMapper.class) {
				TypeLiteral<JsonApiExceptionMapper<?>> typeLiteral = new TypeLiteral<JsonApiExceptionMapper<?>>() {
				};
				type = typeLiteral.getType();
			}

			Set<Bean<?>> beans = beanManager.getBeans(type);
			for (Bean<?> bean : beans) {
				CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);
				T object = (T) beanManager.getReference(bean, type, creationalContext);
				list.add(object);
			}
		}
		return list;
	}

	@Override
	public <A extends Annotation> List<Object> getInstancesByAnnotation(Class<A> annotationClass) {
		List<Object> list = new ArrayList<>();
		BeanManager beanManager = getBeanManager();
		if (beanManager != null) {
			Set<Bean<?>> beans = beanManager.getBeans(Object.class);
			for (Bean<?> bean : beans) {
				Class<?> beanClass = bean.getBeanClass();
				Optional<A> annotation = ClassUtils.getAnnotation(beanClass, annotationClass);
				if (annotation.isPresent()) {
					CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);
					Object object = beanManager.getReference(bean, beanClass, creationalContext);
					list.add(object);
				}
			}
		}
		return list;
	}

}
