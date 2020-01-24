package io.crnk.data.activiti.example.model;

import io.crnk.data.activiti.example.approval.ApprovalValues;
import io.crnk.data.activiti.example.approval.HistoricApprovalProcessInstance;
import io.crnk.core.resource.annotations.JsonApiResource;

// tag::docs1[]
@JsonApiResource(type = "approval/scheduleHistory")
public class HistoricScheduleApprovalProcessInstance extends HistoricApprovalProcessInstance {

	private ScheduleApprovalValues newValues;

	private ScheduleApprovalValues previousValues;

	//@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS)
	//private HistoricApproveTask approveTask;

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

	/*
	public HistoricApproveTask getApproveTask() {
		return approveTask;
	}

	public void setApproveTask(HistoricApproveTask approveTask) {
		this.approveTask = approveTask;
	}
	 */
	// tag::docs2[]
}
// end::docs2[]