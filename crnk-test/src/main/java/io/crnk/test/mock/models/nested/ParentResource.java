package io.crnk.test.mock.models.nested;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;

import java.util.List;

@JsonApiResource(type = "parent")
public class ParentResource {

	@JsonApiId
	private String id;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, opposite = "parent", repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OPPOSITE)
	private List<ManyNestedResource> manyNested;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<ManyNestedResource> getManyNested() {
		return manyNested;
	}

	public void setManyNested(List<ManyNestedResource> manyNested) {
		this.manyNested = manyNested;
	}
}
