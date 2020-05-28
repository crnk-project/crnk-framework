package io.crnk.core.mock.models;

import java.util.List;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.SerializeType;

@JsonApiResource(type = "lazy_tasks")
public class LazyTask {

	@JsonApiId
	private Long id;

	@JsonApiRelation
	private List<Project> projects;

	@JsonApiRelation(serialize = SerializeType.ONLY_ID)
	private Project project;

	@JsonApiRelation
	private Project lazyProject;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<Project> getProjects() {
		return projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Project getLazyProject() {
		return lazyProject;
	}

	public void setLazyProject(Project lazyProject) {
		this.lazyProject = lazyProject;
	}
}
