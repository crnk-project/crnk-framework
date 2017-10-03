package io.crnk.activiti.example.model;

import io.crnk.activiti.example.approval.ApprovalProcessInstance;
import io.crnk.activiti.example.approval.ApprovalValues;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;

// tag::docs1[]
@JsonApiResource(type = "approval/schedule")
public class ScheduleApprovalProcessInstance extends ApprovalProcessInstance {

	private ScheduleApprovalValues newValues;

	private ScheduleApprovalValues previousValues;

	// end::docs1[]

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS)
	private ApproveTask approveTask;

	public ScheduleApprovalValues getNewValues() {
		return newValues;
	}

	public void setNewValues(ApprovalValues newValues) {
		this.newValues = (ScheduleApprovalValues) newValues;
	}

	public ScheduleApprovalValues getPreviousValues() {
		return previousValues;
	}

	public void setPreviousValues(ApprovalValues previousValues) {
		this.previousValues = (ScheduleApprovalValues) previousValues;
	}

	public ApproveTask getApproveTask() {
		return approveTask;
	}

	public void setApproveTask(ApproveTask approveTask) {
		this.approveTask = approveTask;
	}
	// tag::docs2[]
}
// end::docs2[]