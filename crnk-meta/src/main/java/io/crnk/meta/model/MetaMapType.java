package io.crnk.meta.model;

import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.SerializeType;

@JsonApiResource(type = "meta/mapType")
public class MetaMapType extends MetaType {

	@JsonApiRelation(serialize = SerializeType.LAZY)
	private MetaType keyType;

	public MetaType getKeyType() {
		return keyType;
	}

	public void setKeyType(MetaType keyType) {
		this.keyType = keyType;
	}

}
