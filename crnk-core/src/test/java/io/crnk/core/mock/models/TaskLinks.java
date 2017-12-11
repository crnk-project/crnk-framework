package io.crnk.core.mock.models;

import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.links.SelfLinksInformation;

public class TaskLinks implements LinksInformation, SelfLinksInformation {

	public String value = "test";

	private String self;

	@Override
	public String getSelf() {
		return self;
	}

	@Override
	public void setSelf(String self) {
		this.self = self;
	}
}
