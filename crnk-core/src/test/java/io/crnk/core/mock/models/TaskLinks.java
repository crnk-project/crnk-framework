package io.crnk.core.mock.models;

import io.crnk.core.resource.links.DefaultLink;
import io.crnk.core.resource.links.Link;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.links.SelfLinksInformation;

public class TaskLinks implements LinksInformation, SelfLinksInformation {

	public Link value = new DefaultLink("test");

	private Link self;

	@Override
	public Link getSelf() {
		return self;
	}

	@Override
	public void setSelf(Link self) {
		this.self = self;
	}
}
