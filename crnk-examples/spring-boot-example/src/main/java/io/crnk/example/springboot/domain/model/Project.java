package io.crnk.example.springboot.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import java.util.ArrayList;
import java.util.List;

// tag::doc[]
@JsonApiResource(type = "projects")
public class Project {

	@JsonApiId
	private Long id;

	@JsonProperty
	private String name;

	@JsonApiRelation(opposite = "project", lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL,
			repositoryBehavior = RelationshipRepositoryBehavior.IMPLICIT_GET_OPPOSITE_MODIFY_OWNER)
	private List<Task> tasks = new ArrayList<>();

	// end::doc[]

	public Project() {
	}

	public Project(Long id, String name) {
		this.id = id;
		this.name = name;
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

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}
	// tag::doc[]
}
// end::doc[]
