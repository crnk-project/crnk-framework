package io.crnk.test.mock.models;

import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiLinksInformation;
import io.crnk.core.resource.annotations.JsonApiMetaInformation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.links.SelfLinksInformation;
import io.crnk.core.resource.meta.MetaInformation;

@JsonApiResource(type = "simple_tasks", pagingSpec = OffsetLimitPagingSpec.class)
public class SimpleTask {

	@JsonApiId
	private Long id;

	private String name;

	@JsonApiMetaInformation
	private SimpleTaskMeta metaInformation;

	@JsonApiLinksInformation
	private SimpleTaskLinks linksInformation;

	public static class SimpleTaskLinks implements LinksInformation, SelfLinksInformation {

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

	public static class SimpleTaskMeta implements MetaInformation {

		public String value = "test";

	}


	public Long getId() {
		return id;
	}

	public SimpleTask setId(Long id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(@SuppressWarnings("SameParameterValue") String name) {
		this.name = name;
	}

	public SimpleTaskMeta getMetaInformation() {
		return metaInformation;
	}

	public SimpleTask setMetaInformation(SimpleTaskMeta metaInformation) {
		this.metaInformation = metaInformation;
		return this;
	}

	public SimpleTaskLinks getLinksInformation() {
		return linksInformation;
	}

	public SimpleTask setLinksInformation(SimpleTaskLinks linksInformation) {
		this.linksInformation = linksInformation;
		return this;
	}
}
