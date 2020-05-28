package io.crnk.reactive.model;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiLinksInformation;
import io.crnk.core.resource.annotations.JsonApiMetaInformation;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.links.DefaultLink;
import io.crnk.core.resource.links.Link;
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
