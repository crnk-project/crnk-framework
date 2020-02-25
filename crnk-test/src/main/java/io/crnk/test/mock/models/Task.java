package io.crnk.test.mock.models;

import java.util.ArrayList;
import java.util.List;

import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import io.crnk.core.resource.ResourceTypeHolder;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiLinksInformation;
import io.crnk.core.resource.annotations.JsonApiMetaInformation;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.SerializeType;
import io.crnk.core.resource.links.DefaultLink;
import io.crnk.core.resource.links.Link;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.links.SelfLinksInformation;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.MetaInformation;

@JsonApiResource(type = "tasks", pagingSpec = OffsetLimitPagingSpec.class)
public class Task implements ResourceTypeHolder {

	@JsonApiId
	private Long id;

	private String name;

	@JsonApiRelation
	private Project project;

	@JsonApiRelation(serialize = SerializeType.ONLY_ID)
	private Schedule schedule;

	@JsonApiRelation
	private ResourceList<Project> projects;

	@JsonApiRelation
	private Project includedProject;

	@JsonApiRelation
	private List<Project> includedProjects = new ArrayList<>();

	@JsonApiMetaInformation
	private TaskMeta metaInformation = new TaskMeta();

	@JsonApiLinksInformation
	private TaskLinks linksInformation = new TaskLinks();

	private String type;

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	public static class TaskLinks implements LinksInformation, SelfLinksInformation {

		public Link value = new DefaultLink("test");

		public Link self;

		@Override
		public Link getSelf() {
			return self;
		}

		@Override
		public void setSelf(Link self) {
			this.self = self;
		}
	}

	public static class TaskMeta implements MetaInformation {

		public String value = "test";

	}

	private List<Task> otherTasks;

	private TaskStatus status;

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}

	public List<Task> getOtherTasks() {
		return otherTasks;
	}

	public Task setOtherTasks(List<Task> otherTasks) {
		this.otherTasks = otherTasks;
		return this;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	public Long getId() {
		return id;
	}

	public Task setId(Long id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(@SuppressWarnings("SameParameterValue") String name) {
		this.name = name;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public ResourceList<Project> getProjects() {
		return projects;
	}

	public void setProjects(ResourceList<Project> projects) {
		this.projects = projects;
	}

	public Project getIncludedProject() {
		return includedProject;
	}

	public void setIncludedProject(Project includedProject) {
		this.includedProject = includedProject;
	}

	public List<Project> getIncludedProjects() {
		return includedProjects;
	}

	public void setIncludedProjects(List<Project> includedProjects) {
		this.includedProjects = includedProjects;
	}

	public TaskMeta getMetaInformation() {
		return metaInformation;
	}

	public Task setMetaInformation(TaskMeta metaInformation) {
		this.metaInformation = metaInformation;
		return this;
	}

	public TaskLinks getLinksInformation() {
		return linksInformation;
	}

	public Task setLinksInformation(TaskLinks linksInformation) {
		this.linksInformation = linksInformation;
		return this;
	}
}
