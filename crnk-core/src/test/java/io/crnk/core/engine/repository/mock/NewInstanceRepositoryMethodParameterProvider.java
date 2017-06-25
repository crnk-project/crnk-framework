package io.crnk.core.engine.repository.mock;

import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

import java.lang.reflect.Method;

public class NewInstanceRepositoryMethodParameterProvider implements RepositoryMethodParameterProvider {


	@Override
	public <T> T provide(Method method, int parameterIndex) {
		Class<?> aClass = method.getParameterTypes()[parameterIndex];

		try {
			return (T) aClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}