package io.crnk.meta.mock.model;

import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource(type = "extendsResource")
public class ExtendsResource extends ExtendsBaseResource {

	private String childName;

	public String getChildName() {
		return childName;
	}

	public void setChildName(String childName) {
		this.childName = childName;
	}

}
