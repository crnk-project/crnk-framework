package io.crnk.ui.presentation.element;

import io.crnk.core.resource.annotations.JsonApiId;

public class PresentationElement {

	@JsonApiId
	private String id;

	private String componentId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}
}
