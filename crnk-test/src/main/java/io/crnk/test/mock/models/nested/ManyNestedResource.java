package io.crnk.test.mock.models.nested;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiLinksInformation;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.links.DefaultSelfLinksInformation;

@JsonApiResource(type = "nested")
public class ManyNestedResource {

	@JsonApiId
	private NestedId id;

	private String value;

	@JsonApiRelationId
	private String relatedId;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OWNER)
	private NestedRelatedResource related;

	@JsonApiRelation(opposite = "manyNested", lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OWNER)
	private ParentResource parent;

	@JsonApiLinksInformation
	private DefaultSelfLinksInformation links = new DefaultSelfLinksInformation();

	public NestedId getId() {
		return id;
	}

	public void setId(NestedId id) {
		this.id = id;
	}

	public DefaultSelfLinksInformation getLinks() {
		return links;
	}

	public void setLinks(DefaultSelfLinksInformation links) {
		this.links = links;
	}

	public ParentResource getParent() {
		return parent;
	}

	public void setParent(ParentResource parent) {
		this.parent = parent;
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
}
