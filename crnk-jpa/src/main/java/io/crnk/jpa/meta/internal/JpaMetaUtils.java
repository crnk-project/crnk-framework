package io.crnk.jpa.meta.internal;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

public class JpaMetaUtils {

	public static boolean isJpaType(Class<?> type) {
		return type.getAnnotation(Embeddable.class) != null
				|| type.getAnnotation(Entity.class) != null
				|| type.getAnnotation(MappedSuperclass.class) != null;
	}

	public static Class<?> getElementType(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType) type;
			if (paramType.getRawType() instanceof Class && Map.class.isAssignableFrom((Class<?>) paramType.getRawType())) {
				return getElementType(paramType.getActualTypeArguments()[1]);
			}
			if (paramType.getRawType() instanceof Class && Collection.class.isAssignableFrom((Class<?>) paramType.getRawType())) {
				return getElementType(paramType.getActualTypeArguments()[0]);
			}
		}
		throw new IllegalArgumentException(type.toString());
	}
}
