package io.crnk.jpa.internal.query;

import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.meta.model.MetaAttribute;

@JsonApiResource(type = "metaComputedAttribute")
public class MetaComputedAttribute extends MetaAttribute {

	@Override
	public Object getValue(Object dataObject) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setValue(Object dataObject, Object value) {
		throw new UnsupportedOperationException();
	}

}
