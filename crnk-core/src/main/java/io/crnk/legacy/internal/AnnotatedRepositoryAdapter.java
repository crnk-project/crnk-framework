package io.crnk.legacy.internal;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.exception.RepositoryAnnotationNotFoundException;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.legacy.repository.annotations.JsonApiLinks;
import io.crnk.legacy.repository.annotations.JsonApiMeta;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class AnnotatedRepositoryAdapter<T> {

	final Object implementationObject;
	final Class<?> implementationClass;
	final ParametersFactory parametersFactory;

	private Method linksMethod;
	private Method metaMethod;

	public AnnotatedRepositoryAdapter(Object implementationObject, ParametersFactory parametersFactory) {
		this.implementationObject = implementationObject;
		this.implementationClass = implementationObject.getClass();
		this.parametersFactory = parametersFactory;
	}

	public boolean linksRepositoryAvailable() {
		assignLinksMethod();
		return linksMethod != null;
	}

	public LinksInformation getLinksInformation(Iterable<T> resources, QueryAdapter queryAdapter) {
		Class<JsonApiLinks> annotationType = JsonApiLinks.class;
		assignLinksMethod();
		checkIfNotNull(annotationType, linksMethod);

		Object[] methodParameters = parametersFactory
				.buildParameters(new Object[]{resources}, linksMethod, queryAdapter, annotationType);

		return invoke(linksMethod, methodParameters);
	}

	private void assignLinksMethod() {
		if (linksMethod == null) {
			linksMethod = ClassUtils.findMethodWith(implementationClass, JsonApiLinks.class);
		}
	}

	public boolean metaRepositoryAvailable() {
		assignMetaMethod();
		return metaMethod != null;
	}

	public MetaInformation getMetaInformation(Iterable<T> resources, QueryAdapter queryAdapter) {
		Class<JsonApiMeta> annotationType = JsonApiMeta.class;
		assignMetaMethod();
		checkIfNotNull(annotationType, metaMethod);

		Object[] methodParameters = parametersFactory
				.buildParameters(new Object[]{resources}, metaMethod, queryAdapter, annotationType);

		return invoke(metaMethod, methodParameters);
	}

	private void assignMetaMethod() {
		if (metaMethod == null) {
			metaMethod = ClassUtils.findMethodWith(implementationClass, JsonApiMeta.class);
		}
	}

	protected void checkIfNotNull(Class<? extends Annotation> annotationClass, Method foundMethod) {
		if (foundMethod == null) {
			throw new RepositoryAnnotationNotFoundException(
					String.format("Annotation %s for class %s not found", annotationClass, implementationObject.getClass()));
		}
	}

	protected <TYPE> TYPE invokeOperation(Method foundMethod, Class<? extends Annotation> annotationType,
										  Object[] firstParameters) {
		checkIfNotNull(annotationType, foundMethod);
		Object[] methodParameters = parametersFactory
				.buildParameters(firstParameters, foundMethod, annotationType);
		return invoke(foundMethod, methodParameters);
	}

	protected <TYPE> TYPE invokeOperation(Method foundMethod, Class<? extends Annotation> annotationType,
										  Object[] firstParameters, QueryAdapter queryAdapter) {
		checkIfNotNull(annotationType, foundMethod);
		Object[] methodParameters = parametersFactory
				.buildParameters(firstParameters, foundMethod, queryAdapter, annotationType);
		return invoke(foundMethod, methodParameters);
	}

	private <TYPE> TYPE invoke(Method method, Object... args) {
		try {
			return (TYPE) method.invoke(implementationObject, args);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof RuntimeException) {
				throw (RuntimeException) e.getCause();
			} else {
				throw new IllegalStateException(e.getCause());
			}
		}
	}

	public Object getImplementationObject() {
		return implementationObject;
	}
}
