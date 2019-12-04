package io.crnk.ui;

import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.ui.presentation.annotation.PresentationFullTextSearchable;

@JsonApiResource(type = "presentationTask", pagingSpec = OffsetLimitPagingSpec.class)
public class PresentationTask {

	@JsonApiId
	private Long id;

	@PresentationFullTextSearchable
	private String name;

	@JsonApiRelation
	private PresentationProject project;

	private int priority;

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

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

	public PresentationProject getProject() {
		return project;
	}

	public void setProject(PresentationProject project) {
		this.project = project;
	}
}
