package io.crnk.meta.model;

import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource(type = "metaPrimaryKey", resourcePath = "meta/primaryKey")
public class MetaPrimaryKey extends MetaKey {

	private boolean generated;

	public boolean isGenerated() {
		return generated;
	}

	public void setGenerated(boolean generated) {
		this.generated = generated;
	}
}
