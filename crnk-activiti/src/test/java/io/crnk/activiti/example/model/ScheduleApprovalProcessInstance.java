package io.crnk.activiti.example.model;

import io.crnk.activiti.example.approval.ApprovalProcessInstance;
import io.crnk.activiti.example.approval.ApprovalValues;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;

// tag::docs1[]
@JsonApiResource(type = "approval/schedule")
public class ScheduleApprovalProcessInstance extends ApprovalProcessInstance {

	public enum ScheduleStatus {
		DONE,
		SHIPPED
	}

	private ScheduleApprovalValues newValues;

	private ScheduleApprovalValues previousValues;

	private String stringValue;

	private int intValue;

	private ScheduleStatus status;

	public ScheduleStatus getStatus() {
		return status;
	}

	public void setStatus(ScheduleStatus status) {
		this.status = status;
	}


	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

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