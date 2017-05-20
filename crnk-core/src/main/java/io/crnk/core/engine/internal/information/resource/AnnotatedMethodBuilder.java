package io.crnk.core.engine.internal.information.resource;

import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import io.crnk.core.exception.InternalException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Since <a href="https://github.com/FasterXML/jackson-databind/commit/0e4249a2b6cd4ce71a2980b50dcd9765ad03324c">a
 * change</a> there was a change in the constructor parameters of {@link AnnotatedMethod} class. This class provides an
 * interface to allow Crnk work with Jackson version that does and does not have this commit applied.
 */
public class AnnotatedMethodBuilder {
	private static final String CANNOT_FIND_PROPER_CONSTRUCTOR = "Couldn't find proper AnnotatedField constructor";

	public static AnnotatedMethod build(AnnotatedClass annotatedClass, Method method, AnnotationMap annotationMap,
										AnnotationMap[] paramAnnotations) {
		for (Constructor<?> constructor : AnnotatedMethod.class.getConstructors()) {
			try {
				return buildAnnotatedField(annotatedClass, method, annotationMap, paramAnnotations, constructor);
			} catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
				throw new InternalException("Exception while building " + AnnotatedMethod.class.getCanonicalName(), e);
			}
		}
		throw new InternalException(CANNOT_FIND_PROPER_CONSTRUCTOR);
	}

	private static AnnotatedMethod buildAnnotatedField(AnnotatedClass annotatedClass, Method method,
													   AnnotationMap annotationMap, AnnotationMap[] paramAnnotations,
													   Constructor<?> constructor)
			throws IllegalAccessException, InstantiationException, InvocationTargetException {
		Class<?> firstParameterType = constructor.getParameterTypes()[0];
		if (firstParameterType == AnnotatedClass.class ||
				"TypeResolutionContext".equals(firstParameterType.getSimpleName())) {
			return (AnnotatedMethod) constructor.newInstance(annotatedClass, method, annotationMap, paramAnnotations);
		} else {
			throw new InternalException(CANNOT_FIND_PROPER_CONSTRUCTOR);
		}
	}
}
