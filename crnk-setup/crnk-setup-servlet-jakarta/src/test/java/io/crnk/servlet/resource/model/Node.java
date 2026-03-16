package io.crnk.servlet.resource.model;

import java.util.Set;

import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;


@JsonApiResource(type = "nodes")
public class Node extends AbstractResource {

	@JsonApiRelation
	private Node parent;

	@JsonApiRelation
	private Set<Node> children;

	@JsonApiRelation
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
