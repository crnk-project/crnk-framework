package io.crnk.jpa.meta.internal;

import java.lang.reflect.Type;
import javax.persistence.Entity;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.jpa.meta.MetaEntity;

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