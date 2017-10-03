package io.crnk.activiti.example.model;


import io.crnk.activiti.example.approval.ApprovalValues;

public class ScheduleApprovalValues implements ApprovalValues {

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
