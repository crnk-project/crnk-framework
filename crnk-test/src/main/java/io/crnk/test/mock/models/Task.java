package io.crnk.test.mock.models;

import io.crnk.core.resource.annotations.*;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.meta.MetaInformation;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonApiResource(type = "tasks")
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
	private MetaInformation metaInformation;

	@JsonApiLinksInformation
	private LinksInformation linksInformation;

	private List<Task> otherTasks;

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

	public MetaInformation getMetaInformation() {
		return metaInformation;
	}

	public Task setMetaInformation(MetaInformation metaInformation) {
		this.metaInformation = metaInformation;
		return this;
	}

	public LinksInformation getLinksInformation() {
		return linksInformation;
	}

	public Task setLinksInformation(LinksInformation linksInformation) {
		this.linksInformation = linksInformation;
		return this;
	}
}
