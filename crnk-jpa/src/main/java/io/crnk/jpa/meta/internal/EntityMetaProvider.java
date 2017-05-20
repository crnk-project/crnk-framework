package io.crnk.jpa.meta.internal;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.jpa.meta.MetaEntity;
import io.crnk.jpa.meta.MetaJpaDataObject;
import io.crnk.meta.model.MetaElement;

import javax.persistence.Entity;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class EntityMetaProvider extends AbstractEntityMetaProvider<MetaEntity> {

	@Override
	public Set<Class<? extends MetaElement>> getMetaTypes() {
		Set<Class<? extends MetaElement>> set = new HashSet<>();
		set.add(MetaEntity.class);
		return set;
	}

	@Override
	public boolean accept(Type type, Class<? extends MetaElement> metaClass) {
		boolean hasAnnotation = ClassUtils.getRawType(type).getAnnotation(Entity.class) != null;
		boolean hasMetaType = metaClass == MetaElement.class || metaClass == MetaEntity.class || metaClass == MetaJpaDataObject.class;
		return hasAnnotation && hasMetaType;
	}

	@Override
	protected MetaEntity newDataObject() {
		return new MetaEntity();
	}

}