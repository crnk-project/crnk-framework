package io.crnk.core.engine.internal.information.resource;

import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import io.crnk.core.exception.InternalException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Since <a href="https://github.com/FasterXML/jackson-databind/commit/0e4249a2b6cd4ce71a2980b50dcd9765ad03324c">a
 * change</a> there was a change in the constructor parameters of {@link AnnotatedField} class. This class provides an
 * interface to allow Crnk work with Jackson version that does and does not have this commit applied.
 */
public class AnnotatedFieldBuilder {
	private static final String CANNOT_FIND_PROPER_CONSTRUCTOR = "Couldn't find proper AnnotatedField constructor";

	public static AnnotatedField build(AnnotatedClass annotatedClass, Field field, AnnotationMap annotationMap) {
		for (Constructor<?> constructor : AnnotatedField.class.getConstructors()) {
			try {
				return buildAnnotatedField(annotatedClass, field, annotationMap, constructor);
			} catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
				throw new InternalException("Exception while building " + AnnotatedField.class.getCanonicalName(), e);
			}
		}
		throw new InternalException(CANNOT_FIND_PROPER_CONSTRUCTOR);
	}

	private static AnnotatedField buildAnnotatedField(AnnotatedClass annotatedClass, Field field,
													  AnnotationMap annotationMap, Constructor<?> constructor)
			throws IllegalAccessException, InstantiationException, InvocationTargetException {
		Class<?> firstParameterType = constructor.getParameterTypes()[0];
		if (firstParameterType == AnnotatedClass.class ||
				"TypeResolutionContext".equals(firstParameterType.getSimpleName())) {
			return (AnnotatedField) constructor.newInstance(annotatedClass, field, annotationMap);
		} else {
			throw new InternalException(CANNOT_FIND_PROPER_CONSTRUCTOR);
		}
	}
}
