package io.crnk.core.engine.internal.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class Generics {

	private Generics(){
	}

	public static Class<?> getResourceClass(Type genericType, Class baseClass) {
		if (Iterable.class.isAssignableFrom(baseClass)) {
			if (genericType instanceof ParameterizedType) {
				ParameterizedType aType = (ParameterizedType) genericType;
				Type[] fieldArgTypes = aType.getActualTypeArguments();
				if (fieldArgTypes.length == 1 && fieldArgTypes[0] instanceof Class<?>) {
					return (Class) fieldArgTypes[0];
				} else {
					throw new IllegalArgumentException("Wrong type: " + aType);
				}
			} else {
				throw new IllegalArgumentException("The relationship must be parametrized (cannot be wildcard or array): "
						+ genericType);
			}
		}
		return baseClass;
	}
}
