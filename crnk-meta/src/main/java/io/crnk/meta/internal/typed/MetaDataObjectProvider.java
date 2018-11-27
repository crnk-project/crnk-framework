package io.crnk.meta.internal.typed;

import io.crnk.core.engine.information.bean.BeanInformation;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.provider.MetaFilter;

import java.lang.reflect.Type;

public abstract class MetaDataObjectProvider extends MetaDataObjectProviderBase<MetaDataObject> implements MetaFilter {

	@Override
	public MetaElement create(Type type) {
		Class<?> rawClazz = ClassUtils.getRawType(type);
		Class<?> superClazz = rawClazz.getSuperclass();
		MetaElement superMeta = null;
		if (superClazz != Object.class && superClazz != null) {
			superMeta = context.allocate(superClazz);
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
			BeanInformation beanInformation = BeanInformation.get(parent.getImplementationClass());
			Type implementationType = beanInformation.getAttribute(attr.getName()).getType();
			MetaElement metaType = context.allocate(implementationType);
			attr.setType(metaType.asType());
		}
	}

	protected abstract Class<? extends MetaElement> getMetaClass();


	@Override
	public void onInitializing(MetaElement element) {
	}

	@Override
	public MetaElement adjustForRequest(MetaElement element, QueryContext queryContext) {
		return element;
	}
}
