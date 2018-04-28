package io.crnk.core.engine.information.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.crnk.core.utils.Optional;

public class BeanAttributeInformation {

	private String name;

	private Field field;

	private Method getter;

	private Method setter;

	private BeanInformation beanInformation;

	private String jsonName;

	protected BeanAttributeInformation(BeanInformation beanInformation) {
		this.beanInformation = beanInformation;
	}

	public Field getField() {
		return field;
	}

	public Method getGetter() {
		return getter;
	}

	public Method getSetter() {
		return setter;
	}

	protected void setSetter(Method setter) {
		this.setter = setter;
	}

	protected void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	protected void setGetter(Method getter) {
		this.getter = getter;
	}

	protected void setField(Field field) {
		this.field = field;
	}

	public Class<?> getImplementationClass() {
		if (field != null) {
			return field.getType();
		}
		return getter.getReturnType();
	}

	private Map<Class, Optional<?>> annotations = new HashMap<>();

	public <A extends Annotation> Optional<A> getAnnotation(Class<A> annotationClass) {
		if (annotations.containsKey(annotationClass)) {
			return (Optional<A>) annotations.get(annotationClass);
		}

		Optional<A> annotation = Optional.empty();
		if (field != null) {
			annotation = Optional.ofNullable(field.getAnnotation(annotationClass));
		}
		if (getter != null && !annotation.isPresent()) {
			annotation = Optional.ofNullable(getter.getAnnotation(annotationClass));
		}

		if (!annotation.isPresent()) {
			for (BeanInformation interfaceBeanDesc : beanInformation.getImplementedInterfaces()) {
				if (interfaceBeanDesc != null) {
					BeanAttributeInformation interfaceAttrDesc = interfaceBeanDesc.getAttribute(name);
					if (interfaceAttrDesc != null) {
						annotation = interfaceAttrDesc.getAnnotation(annotationClass);
						if (annotation.isPresent()) {
							break;
						}
					}
				}
			}
		}

		if (!annotation.isPresent()) {
			BeanInformation superTypeDescriptor = beanInformation.getSuperType();
			if (superTypeDescriptor != null) {
				BeanAttributeInformation superAttrDesc = superTypeDescriptor.getAttribute(name);
				if (superAttrDesc != null) {
					annotation = superAttrDesc.getAnnotation(annotationClass);
				}
			}
		}

		annotations.put(annotationClass, annotation);

		return annotation;
	}

	public boolean isReadable() {
		return getter != null || field != null;
	}

	public Object getValue(Object bean) {
		try {
			if (getter != null) {
				return getter.invoke(bean);
			} else {
				return field.get(bean);
			}
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}

	public void setValue(Object bean, Object value) {
		try {
			if (setter != null) {
				setter.invoke(bean, value);
			} else {
				field.set(bean, value);
			}
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}

	public String getJsonName() {
		if (jsonName == null) {
			Optional<JsonProperty> annotation = getAnnotation(JsonProperty.class);
			if (annotation.isPresent()) {
				jsonName = annotation.get().value();
			} else {
				jsonName = name;
			}
		}
		return jsonName;
	}

	public Type getImplementationType() {
		if (field != null) {
			return field.getGenericType();
		}
		return getter.getGenericReturnType();
	}
}
