package io.crnk.security.repository;

import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.security.ResourcePermission;

@JsonApiResource(type = "securityCallerPermission", resourcePath = "security/callerPermission")
public class CallerPermission {

	@JsonApiId
	private String id;

	private String resourceType;

	private ResourcePermission permission;

	private FilterSpec dataRoomFilter;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public ResourcePermission getPermission() {
		return permission;
	}

	public void setPermission(ResourcePermission permission) {
		this.permission = permission;
	}

	public FilterSpec getDataRoomFilter() {
		return dataRoomFilter;
	}

	public void setDataRoomFilter(FilterSpec dataRoomFilter) {
		this.dataRoomFilter = dataRoomFilter;
	}
}
