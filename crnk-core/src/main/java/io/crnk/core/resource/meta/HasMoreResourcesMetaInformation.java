package io.crnk.core.resource.meta;

/**
 * Implement this class to provide information whether a next page is available when an
 * offset and limit was specified for the request. The availability of this information
 * triggers the computation of pagination links provided by the
 * {@link io.crnk.core.resource.links.PagedLinksInformation} class. It will trigger the computation of the
 * first, previous and next link, but not the last link. To compute the last link,
 * have a look at {@link PagedMetaInformation} to return a total resource count
 * instead of a simple boolean. But note that computation the total resource count
 * is in many occasions more expensive.
 */
public interface HasMoreResourcesMetaInformation extends MetaInformation {

	Boolean getHasMoreResources();

	void setHasMoreResources(Boolean hasMoreResources);
}
