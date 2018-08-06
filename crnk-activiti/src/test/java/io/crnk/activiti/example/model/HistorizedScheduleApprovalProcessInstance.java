package io.crnk.activiti.example.model;

import io.crnk.activiti.example.approval.ApprovalProcessInstance;
import io.crnk.activiti.example.approval.ApprovalValues;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;

// tag::docs1[]
@JsonApiResource(type = "approval/scheduleHistory")
public class HistorizedScheduleApprovalProcessInstance extends ApprovalProcessInstance {

	private ScheduleApprovalValues newValues;

	private ScheduleApprovalValues previousValues;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS)
	private HistorizedApproveTask approveTask;

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

	public HistorizedApproveTask getApproveTask() {
		return approveTask;
	}

	public void setApproveTask(HistorizedApproveTask approveTask) {
		this.approveTask = approveTask;
	}
	// tag::docs2[]
}
// end::docs2[]