package io.crnk.core.engine.internal.information.resource;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import io.crnk.core.engine.internal.utils.ExceptionUtil;
import io.crnk.core.engine.internal.utils.PreconditionUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * Since Jackson 2.7 there is a change in internal method {@link AnnotatedClass#construct} definition. Since Jackson
 * 2.7 there is <i>AnnotatedClass#construct(JavaType, MapperConfig, ClassIntrospector.MixInResolver)</i>
 * and before there is <i>AnnotatedClass#construct(Class, AnnotationIntrospector, ClassIntrospector.MixInResolver)</i>.
 * <p>
 * This builder purpose is to create an instance of {@link AnnotatedClass} no matter which Jackson version is available.
 */
public class AnnotatedClassBuilder {

	private static final String CONSTRUCT_METHOD_NAME = "construct";

	private static final String CANNOT_FIND_PROPER_METHOD = "Couldn't find proper AnnotatedClass#construct method";

	private AnnotatedClassBuilder() {
	}

	public static AnnotatedClass build(final Class<?> declaringClass, final SerializationConfig serializationConfig) {

		for (final Method method : AnnotatedClass.class.getMethods()) {
			if (CONSTRUCT_METHOD_NAME.equals(method.getName()) &&
					method.getParameterTypes().length == 3) {

				return ExceptionUtil.wrapCatchedExceptions(new Callable<AnnotatedClass>() {
					@Override
					public AnnotatedClass call() throws Exception {
						return buildAnnotatedClass(method, declaringClass, serializationConfig);
					}
				}, "Exception while building AnnotatedClass");
			}
		}
		throw new IllegalStateException(CANNOT_FIND_PROPER_METHOD);
	}

	private static AnnotatedClass buildAnnotatedClass(Method method, Class<?> declaringClass,
													  SerializationConfig serializationConfig)
			throws InvocationTargetException, IllegalAccessException {
		if (method.getParameterTypes()[0] == Class.class) {
			return buildOldAnnotatedClass(method, declaringClass, serializationConfig);
		} else {
			PreconditionUtil.verifyEquals(method.getParameterTypes()[0], JavaType.class, "unexpected method signature for %d", method);
			return buildNewAnnotatedClass(method, declaringClass, serializationConfig);
		}
	}

	private static AnnotatedClass buildNewAnnotatedClass(Method method, Class<?> declaringClass,
														 SerializationConfig serializationConfig)
			throws InvocationTargetException, IllegalAccessException {
		JavaType declaringType = serializationConfig.constructType(declaringClass);
		return (AnnotatedClass) method.invoke(null, declaringType, serializationConfig, serializationConfig);
	}

	private static AnnotatedClass buildOldAnnotatedClass(Method method, Class<?> declaringClass,
														 SerializationConfig serializationConfig)
			throws InvocationTargetException, IllegalAccessException {
		boolean useAnnotations = serializationConfig.isAnnotationProcessingEnabled();
		AnnotationIntrospector aintr = useAnnotations ? serializationConfig.getAnnotationIntrospector() : null;
		return (AnnotatedClass) method.invoke(null, declaringClass, aintr, serializationConfig);
	}
}
