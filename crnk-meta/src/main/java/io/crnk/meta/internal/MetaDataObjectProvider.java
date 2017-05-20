package io.crnk.meta.internal;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public abstract class MetaDataObjectProvider extends MetaDataObjectProviderBase<MetaDataObject> {

	@Override
	public MetaElement createElement(Type type) {
		Class<?> rawClazz = ClassUtils.getRawType(type);
		Class<?> superClazz = rawClazz.getSuperclass();
		MetaElement superMeta = null;
		if (superClazz != Object.class && superClazz != null) {
			superMeta = context.getLookup().getMeta(superClazz, getMetaClass());
		}
		MetaDataObject meta = newDataObject();
		meta.setElementType(meta);
		meta.setName(rawClazz.getSimpleName());
		meta.setImplementationType(type);
		meta.setSuperType((MetaDataObject) superMeta);
		if (superMeta != null) {
			((MetaDataObject) superMeta).addSubType(meta);
		}
		createAttributes(meta);
		return meta;
	}

	protected abstract MetaDataObject newDataObject();

	@Override
	public void onInitialized(MetaElement element) {
		if (element instanceof MetaAttribute && element.getParent().getClass() == getMetaClass()) {
			MetaAttribute attr = (MetaAttribute) element;
			MetaDataObject parent = attr.getParent();
			Type implementationType = PropertyUtils.getPropertyType(parent.getImplementationClass(), attr.getName());
			MetaElement metaType = context.getLookup().getMeta(implementationType, getMetaClass(), true);
			if (metaType == null) {
				metaType = context.getLookup().getMeta(implementationType);
			}
			attr.setType(metaType.asType());
		}
	}

	protected abstract Class<? extends MetaElement> getMetaClass();

	@Override
	public Set<Class<? extends MetaElement>> getMetaTypes() {
		Set<Class<? extends MetaElement>> set = new HashSet<>();
		set.add(getMetaClass());
		return set;
	}

}
