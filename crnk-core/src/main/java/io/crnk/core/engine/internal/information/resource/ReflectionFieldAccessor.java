package io.crnk.core.engine.internal.information.resource;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.information.bean.BeanInformation;
import io.crnk.core.engine.information.resource.ResourceFieldAccessor;
import io.crnk.core.engine.internal.utils.PropertyException;
import io.crnk.core.engine.internal.utils.PropertyUtils;

public class ReflectionFieldAccessor implements ResourceFieldAccessor {

	private final Field privateField;

	private Method getter;

	private Method setter;

	private Field field;

	private Class<?> resourceType;

	private String fieldName;

	private Class<?> fieldType;

	public ReflectionFieldAccessor(Class<?> resourceType, String fieldName, Class<?> fieldType) {
		if (fieldName == null) {
			throw new IllegalArgumentException("no fieldName provided");
		}
		if (resourceType == null) {
			throw new IllegalArgumentException("no resourceType provided");
		}
		if (fieldType == null) {
			throw new IllegalArgumentException("no fieldType provided");
		}
		this.resourceType = resourceType;
		this.fieldName = fieldName;
		this.fieldType = fieldType;

		BeanInformation beanInformation = BeanInformation.get(resourceType);
		BeanAttributeInformation attribute = beanInformation.getAttribute(fieldName);
		if (attribute != null) {
			this.getter = attribute.getGetter();
			this.setter = attribute.getSetter();
			this.field = attribute.getField();
		}
		this.privateField = field;
		if (field != null && !Modifier.isPublic(field.getModifiers())) {
			this.field = null;
		}

	}

	public Field getField() {
		return privateField;
	}

	@Override
	public Object getValue(Object resource) {
		if (resource == null) {
			String message = String.format("Cannot get value %s.%s for null", resourceType.getCanonicalName(), fieldName);
			throw new PropertyException(message, resourceType, fieldName);
		}
		try {
			if (field != null) {
				return field.get(resource);
			}
			else if (getter != null) {
				return getter.invoke(resource);
			}
		}
		catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			throw new PropertyException(e, resourceType, fieldName);
		}

		String message = String.format("Cannot find an getter for %s.%s", resourceType.getCanonicalName(), fieldName);
		throw new PropertyException(message, resourceType, fieldName);
	}

	@Override
	public void setValue(Object resource, Object fieldValue) {
		if (resource == null) {
			String message = String.format("Cannot set value %s.%s for null", resourceType.getCanonicalName(), fieldName);
			throw new PropertyException(message, resourceType, fieldName);
		}
		try {
			Object mappedValue = PropertyUtils.prepareValue(fieldValue, fieldType);
			if (field != null) {
				field.set(resource, mappedValue);
			}
			else if (setter != null) {
				setter.invoke(resource, mappedValue);
			}
			else {
				String message = String.format("Cannot find an setter for %s.%s", resourceType.getCanonicalName(), fieldName);
				throw new PropertyException(message, resourceType, fieldName);
			}
		}
		catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			throw new PropertyException(e, resourceType, fieldName);
		}

	}

}
