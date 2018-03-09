package io.crnk.reactive.model;

import io.crnk.core.resource.annotations.*;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.links.SelfLinksInformation;
import io.crnk.core.resource.meta.MetaInformation;

@JsonApiResource(type = "reactive/task")
public class ReactiveTask {

	@JsonApiId
	private Long id;

	private String name;

	@JsonApiMetaInformation
	private TaskMeta metaInformation;

	@JsonApiLinksInformation
	private TaskLinks linksInformation;

	@JsonApiRelation(opposite = "tasks")
	private ReactiveProject project;

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

	public Long getId() {
		return id;
	}

	public ReactiveTask setId(Long id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TaskMeta getMetaInformation() {
		return metaInformation;
	}

	public ReactiveTask setMetaInformation(TaskMeta metaInformation) {
		this.metaInformation = metaInformation;
		return this;
	}

	public TaskLinks getLinksInformation() {
		return linksInformation;
	}

	public ReactiveTask setLinksInformation(TaskLinks linksInformation) {
		this.linksInformation = linksInformation;
		return this;
	}

	public ReactiveProject getProject() {
		return project;
	}

	public void setProject(ReactiveProject project) {
		this.project = project;
	}
}
