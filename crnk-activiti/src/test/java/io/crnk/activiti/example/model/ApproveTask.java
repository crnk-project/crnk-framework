package io.crnk.activiti.example.model;

import io.crnk.activiti.resource.TaskResource;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;

@JsonApiResource(type = "approval/approveTask")
public class ApproveTask extends TaskResource {

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
	private ApproveForm form;

	public ApproveForm getForm() {
		return form;
	}

	public void setForm(ApproveForm form) {
		this.form = form;
	}
}
