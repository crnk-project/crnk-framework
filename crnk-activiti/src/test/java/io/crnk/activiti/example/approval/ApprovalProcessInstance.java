package io.crnk.activiti.example.approval;

import io.crnk.activiti.resource.ProcessInstanceResource;

// tag::docs1[]
public abstract class ApprovalProcessInstance extends ProcessInstanceResource {

	private String resourceId;

	private String resourceType;

	public String getResourceId() {
		return resourceId;
	}

	// end::docs1[]

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	protected abstract void setNewValues(ApprovalValues newValues);

	protected abstract void setPreviousValues(ApprovalValues newValues);

	protected abstract ApprovalValues getNewValues();

	protected abstract ApprovalValues getPreviousValues();


	// tag::docs2[]
}
// end::docs2[]
