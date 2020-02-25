package io.crnk.test.mock.models;

import java.util.ArrayList;
import java.util.List;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiLinksInformation;
import io.crnk.core.resource.annotations.JsonApiMetaInformation;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.links.DefaultLink;
import io.crnk.core.resource.links.Link;
import io.crnk.core.resource.links.SelfLinksInformation;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.test.mock.models.types.ProjectData;

@JsonApiResource(type = "projects")
public class Project {

	@JsonApiId
	private Long id;

	private String name;

	private String description;

	private ProjectData data;

	@JsonApiRelation
	private List<Task> tasks = new ArrayList<>();

	@JsonApiRelation
	private Task task;

	@JsonApiLinksInformation
	private ProjectLinks links = new ProjectLinks();

	@JsonApiMetaInformation
	private ProjectMeta meta = new ProjectMeta();

	public Long getId() {
		return id;
	}

	public Project setId(Long id) {
		this.id = id;
		return this;
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

	public void setDescription(@SuppressWarnings("SameParameterValue") String description) {
		this.description = description;
	}

	public ProjectData getData() {
		return data;
	}

	public void setData(ProjectData data) {
		this.data = data;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public ProjectLinks getLinks() {
		return links;
	}

	public void setLinks(ProjectLinks links) {
		this.links = links;
	}

	public ProjectMeta getMeta() {
		return meta;
	}

	public void setMeta(ProjectMeta meta) {
		this.meta = meta;
	}

	public static class ProjectLinks implements SelfLinksInformation {

		private Link value;

		private Link self;

		public Link getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = new DefaultLink(value);
		}

		@Override
		public Link getSelf() {
			return self;
		}

		@Override
		public void setSelf(Link self) {
			this.self = self;
		}
	}

	public static class ProjectMeta implements MetaInformation {

		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
}
