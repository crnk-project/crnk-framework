package io.crnk.core.engine.internal.utils;

import io.crnk.core.exception.ResourceException;
import io.crnk.core.utils.Optional;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Provides reflection methods for parsing information about a class.
 */
public class ClassUtils {

	public static String PREFIX_GETTER_IS = "is";

	public static String PREFIX_GETTER_GET = "get";

	private ClassUtils() {
	}

	/**
	 * Returns a list of class fields. Supports inheritance and doesn't return synthetic fields.
	 *
	 * @param beanClass class to be searched for
	 * @return a list of found fields
	 */
	public static List<Field> getClassFields(Class<?> beanClass) {
		Map<String, Field> resultMap = new HashMap<>();
		LinkedList<Field> results = new LinkedList<>();

		Class<?> currentClass = beanClass;
		while (currentClass != null && currentClass != Object.class) {
			for (Field field : currentClass.getDeclaredFields()) {
				if (!field.isSynthetic()) {
					Field v = resultMap.get(field.getName());
					if (v == null) {
						resultMap.put(field.getName(), field);
						results.add(field);
					}
				}
			}
			currentClass = currentClass.getSuperclass();
		}

		return results;
	}

	/**
	 * Returns an instance of bean's annotation
	 *
	 * @param beanClass       class to be searched for
	 * @param annotationClass type of an annotation
	 * @param <T>             type of an annotation
	 * @return an instance of an annotation
	 */
	public static <T extends Annotation> Optional<T> getAnnotation(Class<?> beanClass, Class<T> annotationClass) {
		Class<?> currentClass = beanClass;
		while (currentClass != null && currentClass != Object.class) {
			if (currentClass.isAnnotationPresent(annotationClass)) {
				return Optional.of(currentClass.getAnnotation(annotationClass));
			}
			currentClass = currentClass.getSuperclass();
		}

		return Optional.empty();
	}

	/**
	 * Tries to find a class fields. Supports inheritance and doesn't return synthetic fields.
	 *
	 * @param beanClass class to be searched for
	 * @param fieldName field name
	 * @return a list of found fields
	 */
	public static Field findClassField(Class<?> beanClass, String fieldName) {
		Class<?> currentClass = beanClass;
		while (currentClass != null && currentClass != Object.class) {
			for (Field field : currentClass.getDeclaredFields()) {
				if (field.isSynthetic()) {
					continue;
				}

				if (field.getName().equals(fieldName)) {
					return field;
				}
			}
			currentClass = currentClass.getSuperclass();
		}

		return null;
	}

	public static Method findGetter(Class<?> beanClass, String fieldName) {
		for (Method method : beanClass.getMethods()) {
			if (!isGetter(method)) {
				continue;
			}
			String methodGetterName = getGetterFieldName(method);
			if (StringUtils.isBlank(methodGetterName)) {
				continue;
			}
			if (methodGetterName.equals(fieldName)) {
				return method;
			}
		}
		return null;
	}


	public static String getGetterFieldName(Method getter) {
		int getterPrefixLength = getPropertyGetterPrefixLength(getter);
		if (getterPrefixLength == 0) {
			return null;
		}

		return StringUtils.decapitalize(getter.getName().substring(getterPrefixLength));
	}

	private static boolean isValidBeanGetter(Method getter) {
		// property getters must have non-null return type and zero parameters
		int parameterCount = getter.getParameterTypes().length;
		Class returnType = getter.getReturnType();
		return returnType != null && parameterCount == 0;
	}

	private static int getPropertyGetterPrefixLength(Method getter) {
		if (!isValidBeanGetter(getter)) {
			return 0;
		}

		String name = getter.getName();
		boolean isBooleanReturnType = isBoolean(getter.getReturnType());

		int prefixLength = 0;
		if (isBooleanReturnType && name.startsWith(PREFIX_GETTER_IS)) {
			prefixLength = 2;
		}

		if (name.startsWith(PREFIX_GETTER_GET)) {
			prefixLength = 3;
		}

		// check for methods called "get" and "is", as these aren't valid getters
		if (prefixLength == name.length()) {
			prefixLength = 0;
		}

		return prefixLength;
	}

	private static boolean isBoolean(Class<?> returnType) {
		return boolean.class.equals(returnType) || Boolean.class.equals(returnType);
	}


	public static Method findSetter(Class<?> beanClass, String fieldName, Class<?> fieldType) {
		String upperCaseName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

		try {
			return beanClass.getMethod("set" + upperCaseName, fieldType);
		} catch (NoSuchMethodException e1) {
			return null;
		}
	}

	/**
	 * <p>
	 * Return a list of class getters. Supports inheritance and overriding, that is when a method is found on the
	 * lowest level of inheritance chain, no other method can override it. Supports inheritance and
	 * doesn't return synthetic methods.
	 * <p>
	 * A getter:
	 * <ul>
	 * <li>Starts with an <i>is</i> if returns <i>boolean</i> or {@link Boolean} value</li>
	 * <li>Starts with a <i>get</i> if returns non-boolean value</li>
	 * </ul>
	 *
	 * @param beanClass class to be searched for
	 * @return a list of found getters
	 */
	public static List<Method> getClassGetters(Class<?> beanClass) {
		Map<String, Method> resultMap = new HashMap<>();
		LinkedList<Method> results = new LinkedList<>();

		Class<?> currentClass = beanClass;
		while (currentClass != null && currentClass != Object.class) {
			for (Method method : currentClass.getDeclaredMethods()) {
				if (!method.isSynthetic()) {
					if (isGetter(method)) {
						Method v = resultMap.get(method.getName());
						if (v == null) {
							resultMap.put(method.getName(), method);
							results.add(method);
						}
					}
				}
			}
			currentClass = currentClass.getSuperclass();
		}

		return results;
	}

	/**
	 * Return a list of class setters. Supports inheritance and overriding, that is when a method is found on the
	 * lowest level of inheritance chain, no other method can override it.  Supports inheritance
	 * and doesn't return synthetic methods.
	 *
	 * @param beanClass class to be searched for
	 * @return a list of found getters
	 */
	public static List<Method> getClassSetters(Class<?> beanClass) {
		Map<String, Method> result = new HashMap<>();

		Class<?> currentClass = beanClass;
		while (currentClass != null && currentClass != Object.class) {
			for (Method method : currentClass.getDeclaredMethods()) {
				if (!method.isSynthetic()) {
					if (isSetter(method)) {
						Method v = result.get(method.getName());
						if (v == null) {
							result.put(method.getName(), method);
						}
					}
				}
			}
			currentClass = currentClass.getSuperclass();
		}

		return new LinkedList<>(result.values());
	}

	/**
	 * Return a first occurrence of a method annotated with specified annotation
	 *
	 * @param searchClass     class to be searched
	 * @param annotationClass annotation class
	 * @return annotated method or null
	 */
	public static Method findMethodWith(Class<?> searchClass, Class<? extends Annotation> annotationClass) {
		Method foundMethod = null;
		methodFinder:
		while (searchClass != null && searchClass != Object.class) {
			for (Method method : searchClass.getDeclaredMethods()) {
				if (method.isAnnotationPresent(annotationClass)) {
					foundMethod = method;
					break methodFinder;
				}
			}
			searchClass = searchClass.getSuperclass();
		}

		return foundMethod;
	}

	/**
	 * Create a new instance of a object using a default constructor
	 *
	 * @param clazz new instance class
	 * @param <T>   new instance class
	 * @return new instance
	 */
	public static <T> T newInstance(Class<T> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ResourceException(String.format("couldn't create a new instance of %s", clazz));
		}
	}

	private static boolean isGetter(Method method) {
		return isBooleanGetter(method) || isNonBooleanGetter(method);
	}

	public static boolean isBooleanGetter(Method method) {
		boolean startsWithValidPrefix = getPropertyGetterPrefixLength(method) > 0;

		if (!startsWithValidPrefix || method.getParameterTypes().length != 0) {
			return false;
		}

		return boolean.class.equals(method.getReturnType()) || Boolean.class.equals(method.getReturnType());
	}

	private static boolean isNonBooleanGetter(Method method) {

		if (!method.getName().startsWith("get")) {
			return false;
		}
		if (method.getName().length() < 4) {
			return false;
		}
		if (method.getParameterTypes().length != 0) {
			return false;
		}

		return !void.class.equals(method.getReturnType());
	}

	private static boolean isSetter(Method method) {

		if (!method.getName().startsWith("set")) {
			return false;
		}
		if (method.getName().length() < 4) {
			return false;
		}

		return method.getParameterTypes().length == 1;
	}

	public static Class<?> getRawType(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			return getRawType(((ParameterizedType) type).getRawType());
		} else {
			throw new IllegalStateException("unknown type: " + type);
		}
	}

	public static boolean isPrimitiveType(Class<?> type) {
		boolean isInt = type == byte.class || type == short.class || type == int.class || type == long.class;
		boolean isDecimal = type == short.class || type == double.class;
		return type == boolean.class || isInt || isDecimal;
	}

}
