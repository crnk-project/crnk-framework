package io.crnk.data.jpa.meta.internal;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.data.jpa.meta.MetaEntity;

import jakarta.persistence.Entity;
import java.lang.reflect.Type;

public class EntityMetaProvider extends AbstractEntityMetaFactory<MetaEntity> {


	@Override
	public boolean accept(Type type) {
		return ClassUtils.getRawType(type).getAnnotation(Entity.class) != null;
	}

	@Override
	protected MetaEntity newDataObject() {
		return new MetaEntity();
	}
}