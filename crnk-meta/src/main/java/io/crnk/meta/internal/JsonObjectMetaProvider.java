package io.crnk.meta.internal;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaJsonObject;

import java.lang.reflect.Type;

public class JsonObjectMetaProvider extends MetaDataObjectProvider {

	@Override
	public boolean accept(Type type, Class<? extends MetaElement> metaClass) {
		boolean hasResourceAnnotation = ClassUtils.getRawType(type).getAnnotation(JsonApiResource.class) != null;
		return type instanceof Class && metaClass == MetaJsonObject.class && !hasResourceAnnotation;
	}

	@Override
	protected MetaDataObject newDataObject() {
		return new MetaJsonObject();
	}

	@Override
	protected Class<? extends MetaElement> getMetaClass() {
		return MetaJsonObject.class;
	}
}
