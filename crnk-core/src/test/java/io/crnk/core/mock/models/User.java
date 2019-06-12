package io.crnk.core.mock.models;

import java.util.Collections;
import java.util.List;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiLinksInformation;
import io.crnk.core.resource.annotations.JsonApiMetaInformation;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.SerializeType;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.meta.MetaInformation;

@JsonApiResource(type = "users")
public class User {

	@JsonApiId
	private Long loginId;

	private String name;

	@JsonApiRelation(serialize = SerializeType.EAGER, lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
	private List<Project> assignedProjects = Collections.emptyList();

	@JsonApiRelation(serialize = SerializeType.EAGER, lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
	private List<Task> assignedTasks = Collections.emptyList();

	@JsonApiMetaInformation
	private MetaInformation metaInformation;

	@JsonApiLinksInformation
	private LinksInformation linksInformation;

	public Long getLoginId() {
		return loginId;
	}

	public void setLoginId(Long loginId) {
		this.loginId = loginId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Project> getAssignedProjects() {
		return assignedProjects;
	}

	public void setAssignedProjects(List<Project> assignedProjects) {
		this.assignedProjects = assignedProjects;
	}

	public MetaInformation getMetaInformation() {
		return metaInformation;
	}

	public void setMetaInformation(MetaInformation metaInformation) {
		this.metaInformation = metaInformation;
	}

	public LinksInformation getLinksInformation() {
		return linksInformation;
	}

	public void setLinksInformation(LinksInformation linksInformation) {
		this.linksInformation = linksInformation;
	}

	public List<Task> getAssignedTasks() {
		return assignedTasks;
	}

	public void setAssignedTasks(List<Task> assignedTasks) {
		this.assignedTasks = assignedTasks;
	}
}
