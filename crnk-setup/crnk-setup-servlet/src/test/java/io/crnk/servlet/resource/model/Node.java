package io.crnk.servlet.resource.model;

import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.JsonApiToMany;
import io.crnk.core.resource.annotations.JsonApiToOne;

import java.util.Set;


@JsonApiResource(type = "nodes")
public class Node extends AbstractResource {

	@JsonApiToOne
	private Node parent;

	@JsonApiToMany
	private Set<Node> children;

	@JsonApiToMany
	private Set<NodeComment> nodeComments;

	public Node(Long id, Node parent, Set<Node> children) {
		super(id);
		this.parent = parent;
		this.children = children;
	}

	public Node(Long id, Node parent, Set<Node> children, Set<NodeComment> nodeComments) {
		super(id);
		this.parent = parent;
		this.children = children;
		this.nodeComments = nodeComments;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public Set<Node> getChildren() {
		return children;
	}

	public void setChildren(Set<Node> children) {
		this.children = children;
	}

	public Set<NodeComment> getNodeComments() {
		return nodeComments;
	}

	public void setNodeComments(Set<NodeComment> nodeComments) {
		this.nodeComments = nodeComments;
	}
}
