package io.crnk.jpa.meta.internal;

import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.information.bean.BeanInformation;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
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

	public static BeanAttributeInformation getUniquePrimaryKey(Class<?> entityClass) {
		BeanInformation entityInformation = BeanInformation.get(entityClass);
		List<String> attributeNames = entityInformation.getAttributeNames();

		BeanAttributeInformation pkAttribute = null;
		for (String attributeName : attributeNames) {
			BeanAttributeInformation attribute = entityInformation.getAttribute(attributeName);
			if (attribute.getAnnotation(Id.class).isPresent() || attribute.getAnnotation(EmbeddedId.class).isPresent()) {
				if (pkAttribute != null) {
					throw new IllegalStateException(
							"compount primary keys not yet support, consider use of EmbeddedId for " + entityClass.getName());
				}
				pkAttribute = attribute;
			}
		}
		if (pkAttribute == null) {
			throw new IllegalStateException("no primary key found for " + entityClass.getName() + ", use @Id or @EmbeddedId");
		}
		return pkAttribute;
	}
}
