package io.crnk.meta.internal.resource;

import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.meta.internal.typed.MetaDataObjectProvider;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaJsonObject;

import java.lang.reflect.Type;

public class JsonObjectMetaFactory extends MetaDataObjectProvider {


	@Override
	public boolean accept(Type type) {
		if ((type instanceof Class) && ((Class) type).getAnnotation(JsonApiResource.class) != null) {
			throw new IllegalStateException(
					((Class) type).getName() + " has a @JsonApiResource but has no registered repositories");
		}
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

	@Override
	protected String getMetaName(BeanAttributeInformation attrInformation) {
		return attrInformation.getJsonName();
	}
}
