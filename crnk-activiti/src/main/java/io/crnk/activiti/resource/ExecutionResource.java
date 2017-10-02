package io.crnk.activiti.resource;

import io.crnk.core.resource.annotations.JsonApiField;
import io.crnk.core.resource.annotations.JsonApiId;

public class ExecutionResource {

	@JsonApiId
	@JsonApiField(postable = false, patchable = false)
	private String id;

	@JsonApiField(postable = false, patchable = false, sortable = false)
	private String activityId;

	@JsonApiField(patchable = false, sortable = false)
	private String description;

	@JsonApiField(patchable = false, sortable = false)
	private String name;

	@JsonApiField(patchable = false, sortable = false)
	private String parentId;

	@JsonApiField(patchable = false, sortable = false)
	private String tenantId;

	private boolean ended;

	private boolean suspended;

	public String getActivityId() {
		return activityId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public boolean isEnded() {
		return ended;
	}

	public void setEnded(boolean ended) {
		this.ended = ended;
	}

	public boolean isSuspended() {
		return suspended;
	}

	public void setSuspended(boolean suspended) {
		this.suspended = suspended;
	}
}
