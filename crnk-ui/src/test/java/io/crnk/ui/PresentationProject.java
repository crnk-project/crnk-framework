package io.crnk.ui;

import java.util.List;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.JsonApiVersion;
import io.crnk.ui.presentation.annotation.PresentationFullTextSearchable;
import io.crnk.ui.presentation.annotation.PresentationLabel;

@JsonApiResource(type = "presentationProject")
@JsonApiVersion(min = 1)
public class PresentationProject {

	@JsonApiId
	private Long id;

	@PresentationFullTextSearchable
	@PresentationLabel
	private String name;

	@JsonApiVersion(min = 2)
	private String description;

	@JsonApiRelation(mappedBy = "project")
	private List<PresentationTask> tasks;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<PresentationTask> getTasks() {
		return tasks;
	}

	public void setTasks(List<PresentationTask> tasks) {
		this.tasks = tasks;
	}
}
