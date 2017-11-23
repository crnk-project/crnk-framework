package io.crnk.jpa.meta.internal;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.jpa.meta.MetaMappedSuperclass;

import javax.persistence.MappedSuperclass;
import java.lang.reflect.Type;

public class MappedSuperclassMetaFactory extends AbstractEntityMetaFactory<MetaMappedSuperclass> {


	@Override
	public boolean accept(Type type) {
		return ClassUtils.getRawType(type).getAnnotation(MappedSuperclass.class) != null;
	}

	@Override
	protected MetaMappedSuperclass newDataObject() {
		return new MetaMappedSuperclass();
	}

}