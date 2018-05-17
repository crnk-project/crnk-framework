package io.crnk.core.resource.links;

import com.fasterxml.jackson.annotation.JsonInclude;

public class DefaultSelfLinksInformation implements SelfLinksInformation {

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String self;

	public String getSelf() {
		return self;
	}

	public void setSelf(final String self) {
		this.self = self;
	}
}
