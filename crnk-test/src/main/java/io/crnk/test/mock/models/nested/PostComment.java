package io.crnk.test.mock.models.nested;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiLinksInformation;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.links.DefaultSelfLinksInformation;

// tag::docs[]
@JsonApiResource(type = "comment", nested = true)
public class PostComment {

	@JsonApiId
	private PostCommentId id;

	private String value;

	@JsonApiRelation(opposite = "comments", lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OWNER)
	private Post post;

	// end::docs[]

	@JsonApiRelationId
	private String relatedId;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OWNER)
	private NestedRelatedResource related;

	@JsonApiLinksInformation
	private DefaultSelfLinksInformation links = new DefaultSelfLinksInformation();

	public PostCommentId getId() {
		return id;
	}

	public void setId(PostCommentId id) {
		this.id = id;
	}

	public DefaultSelfLinksInformation getLinks() {
		return links;
	}

	public void setLinks(DefaultSelfLinksInformation links) {
		this.links = links;
	}

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getRelatedId() {
		return relatedId;
	}

	public void setRelatedId(String relatedId) {
		this.relatedId = relatedId;
	}

	public NestedRelatedResource getRelated() {
		return related;
	}

	public void setRelated(NestedRelatedResource related) {
		this.related = related;
	}
	// tag::docs[]
}
// end::docs[]