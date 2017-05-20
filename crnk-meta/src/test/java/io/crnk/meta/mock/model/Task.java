package io.crnk.meta.mock.model;

import io.crnk.core.resource.annotations.*;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.meta.MetaInformation;

@JsonApiResource(type = "tasks")
public class Task {

	@JsonApiId
	private Long id;

	private String name;

	@JsonApiToOne(opposite = "tasks")
	private Schedule schedule;

	@JsonApiMetaInformation
	private TaskMetaInformation metaInformation;

	@JsonApiLinksInformation
	private TaskLinksInformation linksInformation;

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

	public void setName(String name) {
		this.name = name;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	public static class TaskMetaInformation implements MetaInformation {

		public String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public static class TaskLinksInformation implements LinksInformation {

		public String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

}
