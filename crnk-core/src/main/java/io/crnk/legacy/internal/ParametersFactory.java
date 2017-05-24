package io.crnk.legacy.internal;

import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.exception.RepositoryMethodException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.legacy.queryParams.QueryParams;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class ParametersFactory {

	private final RepositoryMethodParameterProvider parameterProvider;

	public ParametersFactory(RepositoryMethodParameterProvider parameterProvider) {
		this.parameterProvider = parameterProvider;
	}

	/**
	 * Source: https://stackoverflow.com/a/80503
	 */
	private static Object[] concatenate(Object[] a, Object[] b) {

		int aLen = a.length;
		int bLen = b.length;

		Object[] newArray = new Object[aLen + bLen];
		System.arraycopy(a, 0, newArray, 0, aLen);
		System.arraycopy(b, 0, newArray, aLen, bLen);

		return newArray;
	}

	/**
	 * Build a list of parameters that can be provided to a method.
	 *
	 * @param firstParameters parameters to be returned as the firsts element in the return array
	 * @param method          document method
	 * @param annotationType  method annotation
	 * @param queryAdapter    Ask remmo
	 * @return array of resolved parameters
	 */
	public Object[] buildParameters(Object[] firstParameters, Method method, QueryAdapter queryAdapter,
									Class<? extends Annotation> annotationType) {
		int parametersLength = method.getParameterTypes().length;
		if (firstParameters.length > 0 && parametersLength < 1) {
			throw new RepositoryMethodException(
					String.format("Method with %s annotation should have at least one parameter.", annotationType));
		}
		int parametersToResolve = parametersLength - firstParameters.length;
		Object[] additionalParameters = new Object[parametersToResolve];
		for (int i = firstParameters.length; i < parametersLength; i++) {
			Class<?> parameterType = method.getParameterTypes()[i];
			if (isQueryType(parameterType)) {
				additionalParameters[i - firstParameters.length] = toQueryObject(queryAdapter, parameterType);
			} else {
				additionalParameters[i - firstParameters.length] = parameterProvider.provide(method, i);
			}
		}

		return concatenate(firstParameters, additionalParameters);
	}

	public boolean isQueryType(Class<?> parameterType) {
		return QueryParams.class.equals(parameterType) || QuerySpec.class.equals(parameterType);
	}

	public Object toQueryObject(QueryAdapter queryAdapter, Class<?> parameterType) {
		if (queryAdapter == null || !isQueryType(parameterType)) {
			return null;
		}
		if (QueryParams.class.equals(parameterType)) {
			return queryAdapter.toQueryParams();
		}
		return queryAdapter.toQuerySpec();
	}

	/**
	 * Build a list of parameters that can be provided to a method.
	 *
	 * @param firstParameters parameters to be returned as the first elements in the return array
	 * @param method          document method
	 * @param annotationType  method annotation
	 * @return array of resolved parameters
	 */
	public Object[] buildParameters(Object[] firstParameters, Method method,
									Class<? extends Annotation> annotationType) {
		int parametersLength = method.getParameterTypes().length;
		if (firstParameters.length > 0 && parametersLength < 1) {
			throw new RepositoryMethodException(
					String.format("Method with %s annotation should have at least one parameter.", annotationType));
		}
		int parametersToResolve = parametersLength - firstParameters.length;
		Object[] additionalParameters = new Object[parametersToResolve];
		for (int i = firstParameters.length; i < parametersLength; i++) {
			additionalParameters[i - firstParameters.length] = parameterProvider.provide(method, i);
		}

		return concatenate(firstParameters, additionalParameters);
	}
}
