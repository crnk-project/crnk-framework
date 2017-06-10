package io.crnk.meta.internal;

import java.lang.reflect.Type;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaJsonObject;

public class JsonObjectMetaProvider extends MetaDataObjectProvider {

	@Override
	public boolean accept(Type type, Class<? extends MetaElement> metaClass) {
		boolean hasResourceAnnotation = ClassUtils.getRawType(type).getAnnotation(JsonApiResource.class) != null;
		if (type instanceof Class && metaClass == MetaJsonObject.class && !hasResourceAnnotation) {
			// a bit tricky as there is no clear maker what a "jsonObject" makes up. So we check whether
			// another provider on the generic MetaElement, which catches Arrays, primitives, lists, etc.
			return !context.getLookup().exists(type, MetaElement.class);
		}
		return false;
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
