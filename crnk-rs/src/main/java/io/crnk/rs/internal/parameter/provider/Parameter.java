package io.crnk.rs.internal.parameter.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class Parameter {

	private final Method method;
	private final int parameterIndex;

	public Parameter(Method method, int parameterIndex) {
		this.method = method;
		this.parameterIndex = parameterIndex;
	}

	public Class<?> getType() {
		return method.getParameterTypes()[parameterIndex];
	}

	public <T extends Annotation> T getAnnotation(Class<T> clazz) {
		Annotation[] annotations = method.getParameterAnnotations()[parameterIndex];
		for (Annotation annotation : annotations) {
			if (clazz.isAssignableFrom(annotation.getClass())) {
				return (T) annotation;
			}
		}

		return null;
	}

	public boolean isAnnotationPresent(Class<? extends Annotation> clazz) {
		Annotation[] annotations = method.getParameterAnnotations()[parameterIndex];
		for (Annotation annotation : annotations) {
			if (clazz.isAssignableFrom(annotation.getClass())) {
				return true;
			}
		}

		return false;
	}
}
