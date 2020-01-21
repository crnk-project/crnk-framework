package io.crnk.core.resource.links;

public interface RelatedLinksInformation extends LinksInformation {

	Link getRelated();

	void setRelated(Link related);

	default void setRelated(String related) {
		setRelated(new DefaultLink(related));
	}
}
