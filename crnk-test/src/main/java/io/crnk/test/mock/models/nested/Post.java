package io.crnk.test.mock.models.nested;

import java.util.List;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;

// tag::docs[]
@JsonApiResource(type = "post")
public class Post {

	@JsonApiId
	private String id;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, opposite = "post", repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OPPOSITE)
	private List<PostComment> comments;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, opposite = "post", repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OPPOSITE)
	private PostHeader header;

	// end::docs[]

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<PostComment> getComments() {
		return comments;
	}

	public void setComments(List<PostComment> comments) {
		this.comments = comments;
	}

	public PostHeader getHeader() {
		return header;
	}

	public void setHeader(PostHeader header) {
		this.header = header;
	}
	// tag::docs[]
}
// end::docs[]