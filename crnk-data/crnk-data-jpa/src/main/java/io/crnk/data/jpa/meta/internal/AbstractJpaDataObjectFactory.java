package io.crnk.data.jpa.meta.internal;

import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.data.jpa.meta.MetaJpaDataObject;
import io.crnk.meta.internal.typed.MetaDataObjectProviderBase;

import jakarta.persistence.Transient;

public abstract class AbstractJpaDataObjectFactory<T extends MetaJpaDataObject> extends MetaDataObjectProviderBase<T> {

	@Override
	protected boolean isIgnored(BeanAttributeInformation information) {
		return super.isIgnored(information) || information.getAnnotation(Transient.class).isPresent();
	}
}
