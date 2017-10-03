package io.crnk.activiti.example.model;

import io.crnk.activiti.resource.FormResource;
import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource(type = "approval/approveForm")
public class ApproveForm extends FormResource {

	private boolean approved;

	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}
}
