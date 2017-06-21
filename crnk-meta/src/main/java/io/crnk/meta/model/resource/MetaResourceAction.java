package io.crnk.meta.model.resource;

import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.meta.model.MetaElement;

@JsonApiResource("meta/resourceAction")
public class MetaResourceAction extends MetaElement {

	private MetaRepositoryActionType actionType;

	public MetaRepositoryActionType getActionType() {
		return actionType;
	}

	public void setActionType(MetaRepositoryActionType actionType) {
		this.actionType = actionType;
	}

	public enum MetaRepositoryActionType {
		REPOSITORY,
		RESOURCE
	}
}
