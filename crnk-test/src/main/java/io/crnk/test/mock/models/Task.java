package io.crnk.test.mock.models;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiIncludeByDefault;
import io.crnk.core.resource.annotations.JsonApiLinksInformation;
import io.crnk.core.resource.annotations.JsonApiLookupIncludeAutomatically;
import io.crnk.core.resource.annotations.JsonApiMetaInformation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.JsonApiToMany;
import io.crnk.core.resource.annotations.JsonApiToOne;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.links.SelfLinksInformation;
import io.crnk.core.resource.meta.MetaInformation;

@JsonApiResource(type = "tasks", pagingSpec = OffsetLimitPagingSpec.class)
public class Task {

	@JsonApiId
	private Long id;

	private String name;

	@JsonApiToOne
	@JsonApiIncludeByDefault
	private Project project;

	@JsonApiToOne(opposite = "tasks")
	private Schedule schedule;

	@JsonApiToMany(lazy = false)
	private List<Project> projects = Collections.emptyList();

	@JsonApiToOne
	@JsonApiLookupIncludeAutomatically
	private Project includedProject;

	@JsonApiToMany
	@JsonApiLookupIncludeAutomatically
	private List<Project> includedProjects;

	@JsonApiMetaInformation
	private TaskMeta metaInformation;

	@JsonApiLinksInformation
	private TaskLinks linksInformation;

	public static class TaskLinks implements LinksInformation, SelfLinksInformation {

		public String value = "test";

		public String self;

		@Override
		public String getSelf() {
			return self;
		}

		@Override
		public void setSelf(String self) {
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
		if (this.schedule != schedule) {
			if (this.schedule != null) {
				this.schedule.getTasks().remove(this);
			}
			if (schedule != null) {
				Set<Task> tasks = schedule.getTasks();
				if (tasks == null || Collections.EMPTY_SET.getClass().isAssignableFrom(tasks.getClass())) {
					tasks = new HashSet<>();
					schedule.setTasks(tasks);
				}
				tasks.add(this);
			}
			this.schedule = schedule;
		}
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

	public List<Project> getProjects() {
		return projects;
	}

	public void setProjects(List<Project> projects) {
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
