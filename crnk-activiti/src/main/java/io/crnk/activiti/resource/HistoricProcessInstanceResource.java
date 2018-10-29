package io.crnk.activiti.resource;

import java.time.OffsetDateTime;

import io.crnk.core.resource.annotations.JsonApiField;
import io.crnk.core.resource.annotations.JsonApiId;

public class HistoricProcessInstanceResource {

	private String businessKey;

	@JsonApiField(patchable = false)
	private String processDefinitionKey;

	@JsonApiId
	@JsonApiField(postable = false, patchable = false)
	private String id;

	@JsonApiField(postable = false, patchable = false, sortable = false)
	private String startActivityId;

	@JsonApiField(postable = false, patchable = false, sortable = false)
	private String endActivityId;

	@JsonApiField(patchable = false, sortable = false)
	private String description;

	@JsonApiField(patchable = false, sortable = false)
	private String name;


	@JsonApiField(patchable = false, sortable = false)
	private String tenantId;


	private OffsetDateTime startTime;

	private OffsetDateTime endTime;

	private Long duration;

	private String startUserId;

	private String deleteReason;

	public OffsetDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(OffsetDateTime startTime) {
		this.startTime = startTime;
	}

	public OffsetDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(OffsetDateTime endTime) {
		this.endTime = endTime;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public String getStartUserId() {
		return startUserId;
	}

	public void setStartUserId(String startUserId) {
		this.startUserId = startUserId;
	}

	public String getDeleteReason() {
		return deleteReason;
	}

	public void setDeleteReason(String deleteReason) {
		this.deleteReason = deleteReason;
	}

	public String getStartActivityId() {
		return startActivityId;
	}

	public void setStartActivityId(String startActivityId) {
		this.startActivityId = startActivityId;
	}

	public String getEndActivityId() {
		return endActivityId;
	}

	public void setEndActivityId(String endActivityId) {
		this.endActivityId = endActivityId;
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

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getBusinessKey() {
		return businessKey;
	}

	public void setBusinessKey(String businessKey) {
		this.businessKey = businessKey;
	}

	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

	public void setProcessDefinitionKey(String processDefinitionKey) {
		this.processDefinitionKey = processDefinitionKey;
	}
}
