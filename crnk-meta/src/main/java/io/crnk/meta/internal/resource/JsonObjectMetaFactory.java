package io.crnk.meta.internal.resource;

import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.meta.internal.typed.MetaDataObjectProvider;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaJsonObject;

import java.lang.reflect.Type;

public class JsonObjectMetaFactory extends MetaDataObjectProvider {


	@Override
	public boolean accept(Type type) {
		PreconditionUtil.assertFalse("should not be resource",
				(type instanceof Class) && ((Class) type).getAnnotation(JsonApiResource.class) != null);

		return true; // lowest priority, accept anything
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
