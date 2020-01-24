package io.crnk.core.engine.internal.information.resource;

import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import io.crnk.core.engine.internal.utils.ExceptionUtil;
import io.crnk.core.engine.internal.utils.PreconditionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

/**
 * Since <a href="https://github.com/FasterXML/jackson-databind/commit/0e4249a2b6cd4ce71a2980b50dcd9765ad03324c">a
 * change</a> there was a change in the constructor parameters of {@link AnnotatedField} class. This class provides an
 * interface to allow Crnk work with Jackson version that does and does not have this commit applied.
 */
public class AnnotatedFieldBuilder {

	// TODO clean this up

	private static final String CANNOT_FIND_PROPER_CONSTRUCTOR = "Couldn't find proper AnnotatedField constructor";

	private AnnotatedFieldBuilder() {
	}

	public static AnnotatedField build(final AnnotatedClass annotatedClass, final Field field,
									   final AnnotationMap annotationMap) {
		final Constructor<?> constructor = AnnotatedField.class.getConstructors()[0];
		return ExceptionUtil.wrapCatchedExceptions(new Callable<AnnotatedField>() {
			@Override
			public AnnotatedField call() throws Exception {
				return buildAnnotatedField(annotatedClass, field, annotationMap, constructor);
			}
		}, "Exception while building AnnotatedField");
	}

	private static AnnotatedField buildAnnotatedField(AnnotatedClass annotatedClass, Field field,
													  AnnotationMap annotationMap, Constructor<?> constructor)
			throws IllegalAccessException, InstantiationException, InvocationTargetException {
		Class<?> firstParameterType = constructor.getParameterTypes()[0];

		PreconditionUtil.verify(firstParameterType == AnnotatedClass.class ||
				TypeResolutionContext.class.equals(firstParameterType), CANNOT_FIND_PROPER_CONSTRUCTOR);
		return (AnnotatedField) constructor.newInstance(annotatedClass, field, annotationMap);
	}
}
