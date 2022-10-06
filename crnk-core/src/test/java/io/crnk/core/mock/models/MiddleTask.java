package io.crnk.core.mock.models;

import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource(type = "middleTask", subTypes = BottomTask.class, resourcePath = "treeTasks")
public abstract class MiddleTask extends TopTask {

	private String publicComment;

	private String privateComment;

	public String getPublicComment() {
		return publicComment;
	}

	public void setPublicComment(final String publicComment) {
		this.publicComment = publicComment;
	}

	public String getPrivateComment() {
		return privateComment;
	}

	public void setPrivateComment(final String privateComment) {
		this.privateComment = privateComment;
	}
}