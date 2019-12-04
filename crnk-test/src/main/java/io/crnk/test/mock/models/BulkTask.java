package io.crnk.test.mock.models;

import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource(type = "bulkTasks", pagingSpec = OffsetLimitPagingSpec.class)
public class BulkTask {

	@JsonApiId
	private Long id;

	private String name;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
