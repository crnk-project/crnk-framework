package io.crnk.core.engine.internal.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.crnk.core.exception.ResourceException;


/**
 * Provides reflection methods for parsing information about a class.
 */
public class ClassUtils {

	public static final String PREFIX_GETTER_IS = "is";

	public static final String PREFIX_GETTER_GET = "get";

	private ClassUtils() {
	}

	public static boolean existsClass(String className) {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			loadClass(classLoader, className);
			return true;
		} catch (IllegalStateException e) {
			return false;
		}
	}

	public static Class<?> loadClass(ClassLoader classLoader, String className) {
		try {
			return classLoader.loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(className);
		}
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
			if (fieldName.equals(methodGetterName)) {
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

		String methodName = "set" + upperCaseName;
		try {
			return beanClass.getMethod(methodName, fieldType);
		} catch (NoSuchMethodException e1) {
			// This is okay, there's just no trivial setter. Carry on searching below.
		}

		Method[] methods = beanClass.getMethods();
		for (Method method : methods) {
			Class<?>[] params = method.getParameterTypes();

			if (methodName.equals(method.getName()) && params.length == 1 && params[0].isAssignableFrom(fieldType)) {
				return method;
			}
		}
		return null;
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
			getDeclaredClassGetters(currentClass, resultMap, results);
			currentClass = currentClass.getSuperclass();
		}

		return results;
	}

	private static void getDeclaredClassGetters(Class<?> currentClass, Map<String, Method> resultMap,
												LinkedList<Method> results) {
		for (Method method : currentClass.getDeclaredMethods()) {
			if (!method.isSynthetic() && isGetter(method)) {
				Method v = resultMap.get(method.getName());
				if (v == null) {
					resultMap.put(method.getName(), method);
					results.add(method);
				}
			}
		}
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
				if (!method.isSynthetic() && isSetter(method)) {
					result.putIfAbsent(method.getName(), method);
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
		while (searchClass != null && searchClass != Object.class) {
			for (Method method : searchClass.getDeclaredMethods()) {
				if (method.isAnnotationPresent(annotationClass)) {
					return method;
				}
			}
			searchClass = searchClass.getSuperclass();
		}
		return null;
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
			throw new ResourceException(String.format("couldn't create a new instance of %s", clazz), e);
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

	/**
	 * Given a type, this method resolves the corresponding raw type.
	 *
	 * This method works if {@code type} is of type {@link Class}, or {@link ParameterizedType}.
	 * Its shortcoming is that it cannot resolve {@link TypeVariable} and will always return {@code Object.class},
	 * not attempting to resolve the concrete type that the variable is to be substituted with.
	 *
	 * Please use tools like {@link io.crnk.core.engine.information.bean.BeanInformation} and
	 * {@link io.crnk.core.engine.information.bean.BeanAttributeInformation} instead.
	 */
	public static Class<?> getRawType(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			return getRawType(((ParameterizedType) type).getRawType());
		} else if (type instanceof TypeVariable<?>) {
			return getRawType(((TypeVariable<?>) type).getBounds()[0]);
		} else if (type instanceof WildcardType) {
			return getRawType(((WildcardType) type).getUpperBounds()[0]);
		}
		throw new IllegalStateException("unknown type: " + type);
	}

	public static boolean isPrimitiveType(Class<?> type) {
		boolean isInt = type == byte.class || type == short.class || type == int.class || type == long.class;
		boolean isDecimal = type == short.class || type == double.class;
		return type == boolean.class || isInt || isDecimal;
	}

	public static Type getElementType(Type genericType) {
		Class rawtype = getRawType(genericType);
		if (Iterable.class.isAssignableFrom(rawtype) && genericType instanceof Class) {
			return Object.class;
		}
		if (Iterable.class.isAssignableFrom(rawtype)) {
			return ((ParameterizedType) genericType).getActualTypeArguments()[0];
		}
		return genericType;
	}
}
