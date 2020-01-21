package io.crnk.core.resource.links;

public interface SelfLinksInformation extends LinksInformation {

	Link getSelf();

	void setSelf(Link self);

	default void setSelf(String self) {
		setSelf(new DefaultLink(self));
	}
}
