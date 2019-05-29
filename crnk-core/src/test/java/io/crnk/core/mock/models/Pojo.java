package io.crnk.core.mock.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource(type = "pojo")
public class Pojo extends Thing {

	@JsonProperty("other-pojo")
	private OtherPojo otherPojo;

	@JsonProperty("some-project")
	@JsonApiRelation
	private Project project;

	@JsonProperty("some-projects")
	@JsonApiRelation
	private List<Project> projects;

	public OtherPojo getOtherPojo() {
		return otherPojo;
	}

	public void setOtherPojo(OtherPojo otherPojo) {
		this.otherPojo = otherPojo;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public List<Project> getProjects() {
		return projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}
}
