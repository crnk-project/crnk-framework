package io.crnk.jpa.meta.internal;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.jpa.meta.MetaJpaDataObject;
import io.crnk.meta.internal.MetaDataObjectProviderBase;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaType;

public abstract class AbstractJpaDataObjectProvider<T extends MetaJpaDataObject> extends MetaDataObjectProviderBase<T> {

	@Override
	public void onInitialized(MetaElement element) {
		super.onInitialized(element);
		if (element.getParent() instanceof MetaJpaDataObject && element instanceof MetaAttribute) {
			MetaAttribute attr = (MetaAttribute) element;
			MetaDataObject parent = attr.getParent();
			Type implementationType = PropertyUtils.getPropertyType(parent.getImplementationClass(), attr.getName());

			Class<?> elementType = getElementType(implementationType);

			boolean jpaObject = attr.isAssociation() || isJpaType(elementType);

			Class<? extends MetaType> metaClass = jpaObject ? MetaJpaDataObject.class : MetaType.class;
			MetaType metaType = context.getLookup().getMeta(implementationType, metaClass);
			PreconditionUtil.assertNotNull("no type available", metaType);

			attr.setType(metaType);
		}
	}

	private boolean isJpaType(Class<?> type) {
		return type.getAnnotation(Embeddable.class) != null
				|| type.getAnnotation(Entity.class) != null
				|| type.getAnnotation(MappedSuperclass.class) != null;
	}

	private Class<?> getElementType(Type type) {
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
