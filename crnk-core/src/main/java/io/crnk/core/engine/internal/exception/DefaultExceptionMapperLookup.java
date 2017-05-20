package io.crnk.core.engine.internal.exception;

import io.crnk.core.engine.error.JsonApiExceptionMapper;
import io.crnk.core.exception.InvalidResourceException;
import io.crnk.legacy.queryParams.errorhandling.ExceptionMapperProvider;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Exception mapper lookup which scans the classpath for exception mappers which
 * are annotated with the {@link ExceptionMapperProvider} annotation.
 */
public class DefaultExceptionMapperLookup implements ExceptionMapperLookup {

	private List<String> resourceSearchPackages;

	public DefaultExceptionMapperLookup(String resourceSearchPackage) {
		this(resourceSearchPackage != null ? Arrays.asList(resourceSearchPackage.split(",")) : null);
	}

	public DefaultExceptionMapperLookup(List<String> resourceSearchPackages) {
		this.resourceSearchPackages = resourceSearchPackages;
	}

	@Override
	public Set<JsonApiExceptionMapper> getExceptionMappers() {
		Reflections reflections;
		if (resourceSearchPackages != null) {
			reflections = new Reflections(resourceSearchPackages);
		} else {
			reflections = new Reflections();
		}
		Set<Class<?>> exceptionMapperClasses = reflections.getTypesAnnotatedWith(ExceptionMapperProvider.class);

		Set<JsonApiExceptionMapper> exceptionMappers = new HashSet<>();
		for (Class<?> exceptionMapperClazz : exceptionMapperClasses) {
			if (!JsonApiExceptionMapper.class.isAssignableFrom(exceptionMapperClazz)) {
				throw new InvalidResourceException(exceptionMapperClazz.getCanonicalName() + " is not an implementation of JsonApiExceptionMapper");
			}
			try {
				exceptionMappers.add((JsonApiExceptionMapper<? extends Throwable>) exceptionMapperClazz.newInstance());
			} catch (Exception e) {
				throw new InvalidResourceException(exceptionMapperClazz.getCanonicalName() + " can not be initialized", e);
			}
		}
		return exceptionMappers;
	}

}
