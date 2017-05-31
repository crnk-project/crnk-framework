package io.crnk.core.engine.internal.information.resource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import io.crnk.core.engine.internal.utils.ExceptionUtil;
import io.crnk.core.engine.internal.utils.PreconditionUtil;

/**
 * Since <a href="https://github.com/FasterXML/jackson-databind/commit/0e4249a2b6cd4ce71a2980b50dcd9765ad03324c">a
 * change</a> there was a change in the constructor parameters of {@link AnnotatedMethod} class. This class provides an
 * interface to allow Crnk work with Jackson version that does and does not have this commit applied.
 */
public class AnnotatedMethodBuilder {

	// TODO clean this up

	private static final String CANNOT_FIND_PROPER_CONSTRUCTOR = "Couldn't find proper AnnotatedField constructor";

	private AnnotatedMethodBuilder() {
	}

	public static AnnotatedMethod build(final AnnotatedClass annotatedClass, final Method method,
			final AnnotationMap annotationMap,
			final AnnotationMap[] paramAnnotations) {

		final Constructor<?> constructor = AnnotatedMethod.class.getConstructors()[0];

		return ExceptionUtil.wrapCatchedExceptions(new Callable<AnnotatedMethod>() {
			@Override
			public AnnotatedMethod call() throws Exception {
				return buildAnnotatedField(annotatedClass, method, annotationMap, paramAnnotations, constructor);
			}
		}, "Exception while building AnnotatedMethod");
	}

	private static AnnotatedMethod buildAnnotatedField(AnnotatedClass annotatedClass, Method method,
			AnnotationMap annotationMap, AnnotationMap[] paramAnnotations,
			Constructor<?> constructor)
			throws IllegalAccessException, InstantiationException, InvocationTargetException {
		Class<?> firstParameterType = constructor.getParameterTypes()[0];

		PreconditionUtil.assertTrue(CANNOT_FIND_PROPER_CONSTRUCTOR,
				firstParameterType == AnnotatedClass.class || TypeResolutionContext.class.equals(firstParameterType));
		return (AnnotatedMethod) constructor.newInstance(annotatedClass, method, annotationMap, paramAnnotations);
	}
}
