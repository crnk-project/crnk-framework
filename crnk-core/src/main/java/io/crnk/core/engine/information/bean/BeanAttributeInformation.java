package io.crnk.core.engine.information.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.crnk.core.utils.Optional;
import net.jodah.typetools.TypeResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class BeanAttributeInformation {

	private String name;

	private Field field;

	private Method getter;

	private Method setter;

	private BeanInformation beanInformation;

	private String jsonName;

	private Map<Class, Optional<?>> annotations = new ConcurrentHashMap<>();

	private Type cachedType;

	private BeanAttributeInformation(BeanInformation beanInformation, String name) {
		this.beanInformation = beanInformation;
		this.name = name;
	}

	protected BeanAttributeInformation(BeanInformation beanInformation, Field field) {
		this(beanInformation, field.getName());
		this.field = field;
	}

	protected BeanAttributeInformation(BeanInformation beanInformation, Method getter, String name) {
		this(beanInformation, name);
		this.getter = getter;
	}

	public BeanInformation getBeanInformation() {
		return beanInformation;
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

	public String getName() {
		return name;
	}

	protected void setGetter(Method getter) {
		if (this.getter != null) {
			throw new IllegalStateException("Getter has already been set, modifications are not allwed.");
		}
		this.getter = getter;
	}

	/**
	 * @return the raw type of this attribute.
	 */
	public Class<?> getImplementationClass() {
		if (field != null) {
			return field.getType();
		}
		return getter.getReturnType();
		// Possible extension to setter with one argument:
		//if (setter != null) {
		//	Class<?>[] parameterTypes = setter.getParameterTypes();
		//	if (parameterTypes.length == 1) {
		//		return parameterTypes[0];
		//	}
		//}
	}

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
		return jsonName.isEmpty() ? name : jsonName;
	}

	/**
	 * Depending on the underlying class member, i.e. whether this
	 * attribute represents a field, getter, or setter (ordered by
	 * precedence), this method will return {@link Field#getGenericType()},
	 * {@link Method#getGenericReturnType()} or the only element of
	 * {@link Method#getGenericParameterTypes()}, respectively.
	 *
	 * The returned type represents the generic type of the
	 * attribute itself. Note that in case the type is
	 * actually not generic, then a plain {@link Class} might be
	 * returned.
	 *
	 * If the concrete type derived from the generic type is required,
	 * consider {@link #getType()}
	 *
	 * @return type of this attribute, without resolving generics
	 */
	public Type getImplementationType() {
		if (field != null) {
			return field.getGenericType();
		}
		return getter.getGenericReturnType();
		// Possible extension to setter with one argument
		//if (setter != null) {
		//	final Type[] parameterTypes = setter.getGenericParameterTypes();
		//	if (parameterTypes.length == 1) {
		//		return parameterTypes[0];
		//	}
		//}
	}

	/**
	 * @return type that is constructed by resolving the actual types
	 * of type variables.
	 */
	public Type getType() {
		if (cachedType == null) {
			cachedType = TypeResolver.reify(getImplementationType(), beanInformation.getImplementationClass());
		}
		return cachedType;
	}

	public boolean isConcretion() {
		if (isDeclaredHere()) {
			return false;
		}

		final BeanInformation superBeanInformation = beanInformation.getSuperType();
		final Type superType = superBeanInformation.getAttribute(getName()).getType();
		return !superType.equals(getType());
	}

	/**
	 * The {@link Member} that represents this bean attribute.
	 */
	private Member getMember() {
		if (field != null) {
			return field;
		}
		return getter;
		// Possible extension to setter
		//if (setter != null) {
		//	return setter;
		//}
	}

	private Class<?> getDeclaringClass() {
		return getMember().getDeclaringClass();
	}

	public boolean isDeclaredHere() {
		return getDeclaringClass() == getBeanInformation().getImplementationClass();
	}
}
