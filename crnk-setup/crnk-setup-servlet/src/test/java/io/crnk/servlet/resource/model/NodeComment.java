package io.crnk.servlet.resource.model;

import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.SerializeType;

@JsonApiResource(type = "node-comments")
public class NodeComment extends AbstractResource {

	private String comment;

	@JsonApiRelation
	private Node parent;

	@JsonApiRelation(serialize = SerializeType.EAGER, lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
	private Locale langLocale;

	public NodeComment(Long id, String comment, Node parent, Locale langLocale) {
		super(id);
		this.comment = comment;
		this.parent = parent;
		this.langLocale = langLocale;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public Locale getLangLocale() {
		return langLocale;
	}

	public void setLangLocale(Locale langLocale) {
		this.langLocale = langLocale;
	}
}
