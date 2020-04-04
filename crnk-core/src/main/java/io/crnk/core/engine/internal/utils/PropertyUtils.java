package io.crnk.core.engine.internal.utils;

import io.crnk.core.exception.RepositoryMethodException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * A lighter version of Apache Commons PropertyUtils without additional dependencies and with support for fluent
 * setters.
 * </p>
 */
public class PropertyUtils {

	private static final PropertyUtils INSTANCE = new PropertyUtils();

	private PropertyUtils() {
	}

	/**
	 * Get bean's property value. The sequence of searches for getting a value is as follows:
	 * <ol>
	 * <li>All class fields are found using {@link ClassUtils#getClassFields(Class)}</li>
	 * <li>Search for a field with the name of the desired one is made</li>
	 * <li>If a field is found and it's a non-public field, the value is returned using the accompanying getter</li>
	 * <li>If a field is found and it's a public field, the value is returned using the public field</li>
	 * <li>If a field is not found, a search for a getter is made - all class getters are found using
	 * {@link ClassUtils#getClassFields(Class)}</li>
	 * <li>From class getters, an appropriate getter with name of the desired one is used</li>
	 * </ol>
	 *
	 * @param bean  bean to be accessed
	 * @param field bean's fieldName
	 * @return bean's property value
	 */
	public static Object getProperty(Object bean, String field) {
		INSTANCE.checkParameters(bean, field);

		try {
			return INSTANCE.getPropertyValue(bean, field);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw handleReflectionException(bean.getClass(), field, e);
		}
	}


	/**
	 * Similar to {@link PropertyUtils#getPropertyClass(Class, List)} but returns the property class.
	 *
	 * @param beanClass bean to be accessed
	 * @param field     bean's fieldName
	 * @return bean's property class
	 */
	public static Class<?> getPropertyClass(Class<?> beanClass, String field) {
		return INSTANCE.findPropertyClass(beanClass, field);
	}

	/**
	 * Similar to {@link PropertyUtils#getPropertyClass(Class, List)} but returns the property class.
	 *
	 * @param beanClass bean to be accessed
	 * @param field     bean's fieldName
	 * @return bean's property class
	 */
	public static Type getPropertyType(Class<?> beanClass, String field) {
		return INSTANCE.findPropertyType(beanClass, field);
	}

	/**
	 * Similar to {@link PropertyUtils#getProperty(Object, String)} but returns the property value for the tail of the given
	 * property path.
	 *
	 * @param bean         bean to be accessed
	 * @param propertyPath property path
	 * @return value
	 */
	public static Object getProperty(Object bean, List<String> propertyPath) {
		Object current = bean;
		for (String propertyName : propertyPath) {
			if (current == null) {
				return null;
			}
			if (current instanceof Iterable) {
				// follow multi-valued property
				List<Object> result = new ArrayList<>();
				Iterable<?> iterable = (Iterable<?>) current;
				for (Object currentElem : iterable) {
					Object property = getProperty(currentElem, propertyName);
					// follow multi-valued nested property
					if (property instanceof Collection) {
						result.addAll((Collection<?>) property);
					} else {
						// follow single-valued nested property
						result.add(property);
					}
				}
				current = result;
			} else {
				// follow single-valued property
				current = getProperty(current, propertyName);
			}
		}
		return current;
	}

	/**
	 * Similar to {@link PropertyUtils#getPropertyClass(Class, String)} but returns the property class for the tail of the given
	 * property path.
	 *
	 * @param clazz        bean to be accessed
	 * @param propertyPath bean's fieldName
	 * @return property class
	 */
	public static Class<?> getPropertyClass(Class<?> clazz, List<String> propertyPath) {
		Class<?> current = clazz;
		for (String propertyName : propertyPath) {
			current = getPropertyClass(current, propertyName);
		}
		return current;
	}

	/**
	 * Set bean's property value. The sequence of searches for setting a value is as follows:
	 * <ol>
	 * <li>All class fields are found using {@link ClassUtils#getClassFields(Class)}</li>
	 * <li>Search for a field with the name of the desired one is made</li>
	 * <li>If a field is found and it's a non-public field, the value is assigned using the accompanying setter</li>
	 * <li>If a field is found and it's a public field, the value is assigned using the public field</li>
	 * <li>If a field is not found, a search for a getter is made - all class getters are found using
	 * {@link ClassUtils#getClassFields(Class)}</li>
	 * <li>From class getters, an appropriate getter with name of the desired one is searched</li>
	 * <li>Using the found getter, an accompanying setter is being used to assign the value</li>
	 * </ol>
	 * <p>
	 * <b>Important</b>
	 * </p>
	 * <ul>
	 * <li>Each setter should have accompanying getter.</li>
	 * <li>If a value to be set is of type {@link List} and the property type is {@link Set}, the collection is changed to {@link
	 * Set}</li>
	 * <li>If a value to be set is of type {@link Set} and the property type is {@link List}, the collection is changed to {@link
	 * List}</li>
	 * </ul>
	 *
	 * @param bean  bean to be accessed
	 * @param field bean's fieldName
	 * @param value value to be set
	 */
	public static void setProperty(Object bean, String field, Object value) {
		INSTANCE.checkParameters(bean, field);

		try {
			INSTANCE.setPropertyValue(bean, field, value);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw handleReflectionException(bean.getClass(), field, e);
		}
	}

	private static RuntimeException handleReflectionException(Class<?> beanClass, String field, ReflectiveOperationException e) {
		if (e instanceof InvocationTargetException &&
				((InvocationTargetException) e).getTargetException() instanceof RuntimeException) {
			return (RuntimeException) ((InvocationTargetException) e).getTargetException();
		}
		return new PropertyException(e, beanClass, field);
	}

	@SuppressWarnings("unchecked")
	public static Object prepareValue(Object value, Class<?> fieldClass) {
		if (Set.class.isAssignableFrom(fieldClass) && value instanceof List) {
			return new LinkedHashSet<>((List) value);
		}
		if (List.class.isAssignableFrom(fieldClass) && value instanceof Set) {
			return new LinkedList<>((Set) value);
		}
		return value;
	}

	private void checkParameters(Object bean, String field) {
		if (bean == null) {
			throw new IllegalArgumentException("No bean specified");
		}
		if (field == null) {
			throw new IllegalArgumentException(String.format("No field specified for bean: %s", bean.getClass()));
		}
	}

	private Object getPropertyValue(Object bean, String fieldName)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

		if (bean instanceof Map) {
			return ((Map) bean).get(fieldName);
		}

		Field foundField = findField(bean.getClass(), fieldName);
		if (foundField != null) {
			if (!Modifier.isPublic(foundField.getModifiers())) {
				Method getter = getGetter(bean.getClass(), foundField.getName());
				return getter.invoke(bean);
			} else {
				return foundField.get(bean);
			}
		} else {
			Method getter = findGetter(bean.getClass(), fieldName);
			checkGetterNotNull(getter, bean.getClass(), fieldName);
			return getter.invoke(bean);
		}
	}

	private void checkGetterNotNull(Method getter, Class<?> beanClass, String fieldName) {
		if (getter == null) {
			String message = String
					.format("Cannot find an getter for %s.%s", beanClass.getCanonicalName(), fieldName);
			throw new PropertyException(message, beanClass, fieldName);
		}
	}

	private Class<?> findPropertyClass(Class<?> beanClass, String fieldName) {
		Field foundField = findField(beanClass, fieldName);
		if (foundField != null) {
			return foundField.getType();
		} else {
			Method getter = findGetter(beanClass, fieldName);
			checkGetterNotNull(getter, beanClass, fieldName);
			return getter.getReturnType();
		}
	}

	private Type findPropertyType(Class<?> beanClass, String fieldName) {
		Field foundField = findField(beanClass, fieldName);
		if (foundField != null) {
			return foundField.getGenericType();
		} else {
			Method getter = findGetter(beanClass, fieldName);
			checkGetterNotNull(getter, beanClass, fieldName);
			return getter.getGenericReturnType();
		}
	}

	private Method findGetter(Class<?> beanClass, String fieldName) {
		List<Method> classGetters = ClassUtils.getClassGetters(beanClass);

		for (Method getter : classGetters) {
			String getterFieldName = ClassUtils.getGetterFieldName(getter);
			if (getterFieldName.equals(fieldName)) {
				return getter;
			}
		}
		return null;
	}

	private Field findField(Class<?> beanClass, String fieldName) {
		List<Field> classFields = ClassUtils.getClassFields(beanClass);
		for (Field field : classFields) {
			if (field.getName().equals(fieldName)) {
				return field;
			}
		}
		return null;
	}

	private Method getGetter(Class<?> beanClass, String fieldName) {
		Method getter = ClassUtils.findGetter(beanClass, fieldName);
		if (getter != null) {
			return getter;
		} else {
			throw new RepositoryMethodException(
					String.format("Unable to find accessor method for %s.%s", beanClass.getName(), fieldName));
		}
	}

	private void setPropertyValue(Object bean, String fieldName, Object value)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Field foundField = findField(bean.getClass(), fieldName);

		if (foundField != null) {
			if (!Modifier.isPublic(foundField.getModifiers())) {
				Method setter = getSetter(bean, foundField.getName(), foundField.getType());
				try {
					setter.invoke(bean, prepareValue(value, setter.getParameterTypes()[0]));
				} catch (IllegalArgumentException e) {
					throw new IllegalStateException(setter.toString() + " with value " + value, e);
				}
			} else {
				foundField.set(bean, prepareValue(value, foundField.getType()));
			}
		} else {
			Method getter = findGetter(bean.getClass(), fieldName);
			checkGetterNotNull(getter, bean.getClass(), fieldName);
			String getterFieldName = ClassUtils.getGetterFieldName(getter);
			Method setter = getSetter(bean, getterFieldName, getter.getReturnType());
			setter.invoke(bean, prepareValue(value, setter.getParameterTypes()[0]));
		}
	}

	private Method getSetter(Object bean, String fieldName, Class<?> fieldType) {
		Class<? extends Object> beanClass = bean.getClass();
		Method setter = ClassUtils.findSetter(beanClass, fieldName, fieldType);
		if (setter != null) {
			return setter;
		} else {
			throw new RepositoryMethodException(
					String.format("Unable to find accessor method for %s.%s", beanClass.getName(), fieldName));
		}
	}
}
