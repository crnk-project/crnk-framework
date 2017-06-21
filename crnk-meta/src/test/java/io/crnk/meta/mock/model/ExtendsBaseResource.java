package io.crnk.meta.mock.model;

import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource("extendsBase")
public class ExtendsBaseResource extends BaseObject {

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
