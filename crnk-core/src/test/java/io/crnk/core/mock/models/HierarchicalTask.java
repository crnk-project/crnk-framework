package io.crnk.core.mock.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;

import java.util.List;

@JsonApiResource(type = "hierarchicalTask")
@JsonPropertyOrder(alphabetic = true)
public class HierarchicalTask {

	@JsonApiId
	private Long id;

	private String name;

	@JsonApiRelation(opposite = "children")
	private HierarchicalTask parent;

	@JsonProperty("another-parent")
	@JsonApiRelation(lookUp = LookupIncludeBehavior.NONE)
	private HierarchicalTask anotherParent;

	@JsonApiRelation(opposite = "parent", lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS)
	private List<HierarchicalTask> children;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HierarchicalTask getParent() {
		return parent;
	}

	public void setParent(HierarchicalTask parent) {
		this.parent = parent;
	}

	public HierarchicalTask getAnotherParent() {
		return anotherParent;
	}

	public void setAnotherParent(HierarchicalTask parent) {
		this.anotherParent = parent;
	}

	public List<HierarchicalTask> getChildren() {
		return children;
	}

	public void setChildren(List<HierarchicalTask> children) {
		this.children = children;
	}
}
