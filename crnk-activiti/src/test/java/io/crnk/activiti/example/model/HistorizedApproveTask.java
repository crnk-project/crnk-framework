package io.crnk.activiti.example.model;

import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.SerializeType;

@JsonApiResource(type = "approval/approveTaskHistory")
public class HistorizedApproveTask extends ApproveTask {

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
	private ApproveForm form;

	@JsonApiRelationId
	private String processInstanceId;

	@JsonApiRelation(serialize = SerializeType.ONLY_ID, lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
	private ScheduleApprovalProcessInstance processInstance;

	public ApproveForm getForm() {
		return form;
	}

	public void setForm(ApproveForm form) {
		this.form = form;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public ScheduleApprovalProcessInstance getProcessInstance() {
		return processInstance;
	}

	public void setProcessInstance(ScheduleApprovalProcessInstance processInstance) {
		this.processInstance = processInstance;
	}
}
